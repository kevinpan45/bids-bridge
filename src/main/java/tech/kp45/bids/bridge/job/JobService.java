package tech.kp45.bids.bridge.job;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.common.exception.BasicRuntimeException;
import tech.kp45.bids.bridge.dataset.Dataset;
import tech.kp45.bids.bridge.dataset.DatasetService;
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

    public Job create(String name, String group, Integer pipelineId, Integer datasetId) {
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

    public Job create(Integer pipelineId, Integer datasetId, List<String> fileRegexes) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String schedule(Integer jobId) {
        Job job = jobMapper.selectById(jobId);
        String engineJobId = jobEngine.submit(job);

        job.setEngineJobId(engineJobId);
        job.setStatus(JobStatus.RUNNING.name());
        jobMapper.updateById(job);
        log.info("Job {} started", jobId);

        return engineJobId;
    }

    public Job finished(Integer jobId) {
        Job job = jobMapper.selectById(jobId);
        job.setStatus(JobStatus.FINISHED.name());
        jobMapper.updateById(job);
        log.info("Job {} finished", jobId);
        return job;
    }

    public void manualStop(Integer jobId) {
        Job job = jobMapper.selectById(jobId);
        jobEngine.stop(job.getEngineJobId());

        job.setStatus(JobStatus.MANUAL_STOPPED.name());
        jobMapper.updateById(job);
        log.info("Job {} manually stopped", jobId);
    }

    public void abnormalStop(Integer jobId) {
        Job job = jobMapper.selectById(jobId);
        job.setStatus(JobStatus.ABNORMAL_STOPPED.name());
        jobMapper.updateById(job);
        log.info("Job {} stopped abnormally", jobId);
    }

    public void delete(Integer jobId) {
        Job job = jobMapper.selectById(jobId);
        if (JobStatus.RUNNING.name().equals(job.getStatus())) {
            // Asynchronously stop the running job in engine.
            new Thread() {
                @Override
                public void run() {
                    jobEngine.stop(job.getEngineJobId());
                }
            }.start();
        }
        job.setDeleted(true);
        jobMapper.updateById(job);
        log.info("Job {} deleted", jobId);
    }

    public List<Job> getRunningJobs() {
        LambdaQueryWrapper<Job> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Job::getStatus, JobStatus.RUNNING.name());
        return jobMapper.selectList(queryWrapper);
    }

    public List<Job> list() {
        return jobMapper.selectList(null);
    }
}
