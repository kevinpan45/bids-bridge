package tech.kp45.bids.bridge.job;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.bff.JobView;
import tech.kp45.bids.bridge.common.exception.BasicRuntimeException;
import tech.kp45.bids.bridge.dataset.Dataset;
import tech.kp45.bids.bridge.dataset.DatasetService;
import tech.kp45.bids.bridge.iam.entity.User;
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
        job.setStatus(JobStatus.FINISHED.name());
        jobMapper.updateById(job);
        log.info("Job {} finished", jobId);
        return job;
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

    public Job get(Integer id) {
        return jobMapper.selectById(id);
    }

    public List<JobView> listJobViews(String group) {
        if (StrUtil.isBlank(group)) {
            group = null;
        }
        return jobMapper.listJobViews(group);
    }

    public Job findByEngineJobId(String engineJobId) {
        LambdaQueryWrapper<Job> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Job::getEngineJobId, engineJobId);
        return jobMapper.selectOne(queryWrapper);
    }
}
