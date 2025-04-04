package tech.kp45.bids.bridge.bff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.job.Job;
import tech.kp45.bids.bridge.job.JobService;
import tech.kp45.bids.bridge.job.JobStatus;
import tech.kp45.bids.bridge.job.scheduler.argo.ArgoEngine;

@Slf4j
@Configuration
public class OpenNeuroCollectionTracker {

    public static final String OPENNEURO_COLLECTION_TRACKER_PREFIX = "openneuro:collection:tracker:";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private ArgoEngine argoEngine;
    @Autowired
    private JobService jobService;

    @Scheduled(cron = "0 */5 * * * *")
    public void trackCollection() {
        redisTemplate.keys(OPENNEURO_COLLECTION_TRACKER_PREFIX + "*").forEach(key -> {
            String engineJobId = key.substring(OPENNEURO_COLLECTION_TRACKER_PREFIX.length());
            if (log.isDebugEnabled()) {
                log.debug("Check job {} status", engineJobId);
            }
            if (argoEngine.exist(engineJobId)) {
                JobStatus status = argoEngine.getStatus(engineJobId);
                if (status == JobStatus.FINISHED || status == JobStatus.FAILED) {
                    log.info("Job {} is end with status {}", engineJobId, status);

                    // Update job status in the database
                    Job job = jobService.findByEngineJobId(engineJobId);
                    if (job != null) {
                        if (status == JobStatus.FINISHED) {
                            jobService.finished(job.getId());
                        } else if (status == JobStatus.FAILED) {
                            jobService.abnormalStop(job.getId());
                        }
                    }

                    // Delete the Redis key after updating the job status
                    redisTemplate.delete(key);
                }
            }
        });
    }
}
