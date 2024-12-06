package tech.kp45.bids.bridge.job;

public enum JobStatus {
    /**
     * Job is created in the platform.
     */
    CREATED,
    /**
     * Job is submitted to the priority queue, waiting for the scheduler to execute.
     */
    SUBMITTED,
    /**
     * Job is running in job engine.
     */
    RUNNING,
    /**
     * Job is finished successfully.
     */
    FINISHED,
    /**
     * Job is stopped manually.
     */
    MANUAL_STOPPED,
    /**
     * Job is stopped abnormally.
     */
    ABNORMAL_STOPPED,
    DELETED,
    /**
     * Job is failed in job engine.
     */
    FAILED
}
