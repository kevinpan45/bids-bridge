package tech.kp45.bids.bridge.collection;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.job.JobStatus;
import tech.kp45.bids.bridge.job.scheduler.argo.ArgoEngine;

/**
 * Scheduled to track OpenNeuro collection task status.
 */
@Slf4j
@Configuration
public class OpenNeuroCollectionTracker {
    @Autowired
    private ArgoEngine argoEngine;
    @Autowired
    private CollectionService collectionService;

    @Scheduled(cron = "0 */5 * * * *")
    public void trackCollection() {
        List<Collection> collections = collectionService.findByStatus(CollectionStatus.IN_PROGRESS);
        if (collections.isEmpty()) {
            return;
        }

        for (Collection collection : collections) {
            String engineJobId = collection.getCollectionExecutionId();
            if (log.isDebugEnabled()) {
                log.debug("Check job {} status", engineJobId);
            }
            if (argoEngine.exist(engineJobId)) {
                JobStatus status = argoEngine.getStatus(engineJobId);
                if (status == JobStatus.FINISHED || status == JobStatus.FAILED) {
                    log.info("Job {} is end with status {}", engineJobId, status);
                    if (status == JobStatus.FINISHED) {
                        collection.setStatus(CollectionStatus.CANCELLED.name());
                        collectionService.update(collection);
                        log.info("Collection {} is finished.", collection.getCollectionExecutionId());
                    } else if (status == JobStatus.FAILED) {
                        collection.setStatus(CollectionStatus.FAILED.name());
                        collectionService.update(collection);
                        log.error("Collection {} is failed.", collection.getCollectionExecutionId());
                    }
                }
            } else {
                log.warn("Job {} is not found in Argo Engine, mark as cancelled", engineJobId);
                // If the job is not found, mark the collection as cancelled
                collection.setStatus(CollectionStatus.CANCELLED.name());
                collection.setDescription("Collection job was cancelled because the collection execution " + engineJobId
                        + " is not found.");
                collectionService.update(collection);
            }
        }
    }
}
