package tech.kp45.bids.bridge.job.scheduler;

import java.util.List;

import tech.kp45.bids.bridge.job.Job;
import tech.kp45.bids.bridge.job.JobStatus;
import tech.kp45.bids.bridge.storage.Storage;

public abstract class JobEngine {
    public abstract String submit(Job job);

    public abstract void stop(String engineJobId);

    public abstract void delete(String engineJobId);

    /**
     * Get the status of the job with the given engineJobId.
     * 
     * @param engineJobId
     * @return
     */
    public abstract JobStatus getStatus(String engineJobId);

    /**
     * Check if the pipeline with the given enginePipelineId is deployed in the job
     * engine.
     * 
     * @param enginePipelineId
     * @return
     */
    public abstract boolean deployed(String enginePipelineId);

    /**
     * Check if the job with the given engineJobId exists in the job engine.
     * 
     * @param engineJobId
     * @return
     */
    public abstract boolean exist(String engineJobId);

    /**
     * Get the list of pipelines that deployed in job engine.
     * 
     * @return
     */
    public abstract List<String> getPipelines();

    /**
     * Setup the storage for the job engine to load dataset and upload artifact.
     * @param storage
     */
    public abstract void setupStorage(Storage storage);

    /**
     * Check if the storage is available for the job engine to load dataset and upload artifact.
     * @return
     */
    public abstract boolean storageAvailable();
}
