package tech.kp45.bids.bridge.job;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.bff.JobView;
import tech.kp45.bids.bridge.common.exception.BasicRuntimeException;
import tech.kp45.bids.bridge.dataset.Dataset;
import tech.kp45.bids.bridge.dataset.DatasetService;
import tech.kp45.bids.bridge.iam.entity.User;
import tech.kp45.bids.bridge.job.artifact.Artifact;
import tech.kp45.bids.bridge.job.artifact.ArtifactService;
import tech.kp45.bids.bridge.job.artifact.ArtifactStatus;
import tech.kp45.bids.bridge.job.scheduler.JobEngine;
import tech.kp45.bids.bridge.pipeline.Pipeline;
import tech.kp45.bids.bridge.pipeline.PipelineService;

@Slf4j
@Service
public class JobService {
    @Autowired
    private PipelineService pipelineService;
    @Autowired
    private DatasetService datasetService;
    @Autowired
    private JobEngine jobEngine;
    @Autowired
    private JobMapper jobMapper;
    @Autowired
    private ArtifactService artifactService;

    public Job create(String name, String group, Integer pipelineId, Integer datasetId, User user) {
        Job job = new Job();
        Pipeline pipeline = pipelineService.get(pipelineId);
        if (pipeline == null || pipeline.isDeleted()) {
            throw new RuntimeException("pipeline not found");
        }

        Dataset dataset = datasetService.get(datasetId);
        if (dataset == null || dataset.isDeleted()) {
            throw new RuntimeException("dataset not found");
        }

        job.setName(name).setGroup(group).setPipelineId(pipelineId).setDatasetId(datasetId);
        if (!user.isEmailVerified()) {
            log.error("User {} is not email verified, cannot create job", user.getEmail());
            throw new BasicRuntimeException("User " + user.getEmail()
                    + " is not email verified, cannot create job, please verify your email first.");
        }
        job.setCreatedBy(user.getEmail());
        jobMapper.insert(job);

        log.info("Job {} of group {} with pipeline {} and dataset {} created", job.getName(), job.getGroup(),
                job.getPipelineId(), job.getDatasetId());

        try {
            // Auto schedule the job, you can manually schedule it if auto schedule failed.
            schedule(job.getId());
        } catch (Exception e) {
            log.error("Job {} failed to schedule", job.getId(), e);
            throw new BasicRuntimeException("Job " + job.getId() + " created but failed to schedule");
        }

        return job;
    }

    public Job create(Integer pipelineId, Integer datasetId, List<String> fileRegexes, User user) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String schedule(Integer jobId) {
        Job job = jobMapper.selectById(jobId);
        if (job == null || job.isDeleted()) {
            log.error("Job {} is deleted and cannot be scheduled", jobId);
            throw new BasicRuntimeException("Job is deleted and cannot be scheduled.");
        }
        String engineJobId = jobEngine.submit(job);

        job.setEngineJobId(engineJobId);
        job.setStatus(JobStatus.RUNNING.name());
        jobMapper.updateById(job);
        log.info("Job {} started", jobId);

        return engineJobId;
    }

    public Job finished(Integer jobId) {
        Job job = jobMapper.selectById(jobId);
        if (job == null || job.isDeleted()) {
            log.error("Job {} not found or deleted", jobId);
            throw new BasicRuntimeException("Job not found or deleted.");
        }
        
        // Create artifact for the finished job if it doesn't exist
        Artifact artifact = createArtifactForJob(job);
        
        // Update job status and artifact reference in a single update
        job.setStatus(JobStatus.FINISHED.name());
        if (artifact != null) {
            job.setArtifactId(artifact.getId());
            log.info("Job {} finished and linked to artifact {}", jobId, artifact.getId());
        } else {
            log.warn("Job {} finished but artifact creation failed", jobId);
        }
        jobMapper.updateById(job);
        
        log.info("Job {} finished", jobId);
        return job;
    }

    private Artifact createArtifactForJob(Job job) {
        try {
            // Check if artifact already exists for this job
            Artifact existingArtifact = artifactService.findByJob(job.getId());
            if (existingArtifact != null) {
                log.debug("Artifact already exists for job {}", job.getId());
                artifactService.deleteById(existingArtifact.getId());
                log.info("Deleted existing artifact for job {}", job.getId());
            }

            Artifact artifact = new Artifact();
            artifact.setJobId(job.getId());

            // Use the engine job ID as the storage path (where job outputs are stored)
            if (StringUtils.hasText(job.getEngineJobId())) {
                artifact.setStoragePath(job.getEngineJobId());
                artifact.setStatus(ArtifactStatus.UPLOADED.name());
            } else {
                // Mark artifact as not found status.
                artifact.setStatus(ArtifactStatus.NOT_FOUND.name());
            }

            artifactService.create(artifact);
            log.info("Created artifact for finished job {} with storage path: {}",
                    job.getId(), artifact.getStoragePath());

            return artifact;

        } catch (Exception e) {
            log.error("Failed to create artifact for job {}", job.getId(), e);
            // Don't throw exception here to avoid failing the job completion
            return null;
        }
    }

    public void manualStop(Integer jobId) {
        Job job = jobMapper.selectById(jobId);
        if (job == null || job.isDeleted()) {
            log.error("Job {} not found or deleted", jobId);
            throw new BasicRuntimeException("Job not found or deleted.");
        }
        jobEngine.stop(job.getEngineJobId());

        job.setStatus(JobStatus.MANUAL_STOPPED.name());
        jobMapper.updateById(job);
        log.info("Job {} manually stopped", jobId);
    }

    public void abnormalStop(Integer jobId) {
        Job job = jobMapper.selectById(jobId);
        if (job == null || job.isDeleted()) {
            log.error("Job {} not found or deleted", jobId);
            throw new BasicRuntimeException("Job not found or deleted.");
        }
        job.setStatus(JobStatus.ABNORMAL_STOPPED.name());
        jobMapper.updateById(job);
        log.info("Job {} stopped abnormally", jobId);
    }

    public void delete(Integer jobId) {
        Job job = jobMapper.selectById(jobId);
        if (job == null || job.isDeleted()) {
            return;
        }
        job.setDeleted(true);
        jobMapper.updateById(job);
        log.info("Job {} deleted", jobId);
        new Thread() {
            @Override
            public void run() {
                jobEngine.delete(job.getEngineJobId());
            }
        }.start();
    }

    public List<Job> getRunningJobs() {
        LambdaQueryWrapper<Job> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Job::getStatus, JobStatus.RUNNING.name()).eq(Job::isDeleted, false);
        return jobMapper.selectList(queryWrapper);
    }

    public List<Job> list() {
        LambdaQueryWrapper<Job> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Job::isDeleted, false).orderByDesc(Job::getCreatedAt);
        return jobMapper.selectList(queryWrapper);
    }

    public List<Job> listByUser(String userEmail) {
        LambdaQueryWrapper<Job> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Job::getCreatedBy, userEmail).eq(Job::isDeleted, false).orderByDesc(Job::getCreatedAt);
        return jobMapper.selectList(queryWrapper);
    }

    public Job get(Integer id) {
        return jobMapper.selectById(id);
    }

    public List<JobView> listViews(String group) {
        if (StrUtil.isBlank(group)) {
            group = null;
        }
        return jobMapper.listViews(group);
    }

    public List<JobView> listViewsByUser(String group, String userEmail) {
        if (StrUtil.isBlank(userEmail)) {
            throw new BasicRuntimeException("User email cannot be blank");
        }
        return jobMapper.listViewsByUser(group, userEmail);
    }

    public Job findByEngineJobId(String engineJobId) {
        LambdaQueryWrapper<Job> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Job::getEngineJobId, engineJobId);
        return jobMapper.selectOne(queryWrapper);
    }
}
