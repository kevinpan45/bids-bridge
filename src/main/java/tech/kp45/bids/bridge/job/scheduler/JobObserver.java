package tech.kp45.bids.bridge.job.scheduler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.job.Job;
import tech.kp45.bids.bridge.job.JobStatus;
import tech.kp45.bids.bridge.job.service.JobService;

/**
 * Observe the status change of running jobs, update job status if job is finished/failed/timeout/missing.
 */
@Slf4j
@Service
public class JobObserver {
    @Autowired
    private JobService jobService;
    @Autowired
    private JobEngine jobEngine;

    @Scheduled(cron = "0 0/1 * * * ?")
    public void observe() {
        List<Job> runningJobs = jobService.getRunningJobs();
        if (runningJobs.isEmpty()) {
            return;
        }

        for (Job job : runningJobs) {
            String engineJobId = job.getEngineJobId();
            if (StringUtils.hasText(engineJobId)) {
                boolean exist = jobEngine.exist(engineJobId);
                if (exist) {
                    JobStatus status = jobEngine.getStatus(job.getEngineJobId());
                    switch (status) {
                        case JobStatus.FINISHED:
                            jobService.finished(job.getId());
                            break;
                        case JobStatus.FAILED:
                            jobService.abnormalStop(job.getId());
                            break;
                        default:
                            break;
                    }
                } else {
                    jobService.abnormalStop(job.getId());
                }
            } else {
                log.error("Job {} has no engine job id", job.getId());
                jobService.abnormalStop(job.getId());
            }
        }
    }
}
