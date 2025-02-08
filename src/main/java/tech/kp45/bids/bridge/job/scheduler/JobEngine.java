package tech.kp45.bids.bridge.job.scheduler;

import tech.kp45.bids.bridge.job.Job;
import tech.kp45.bids.bridge.job.JobStatus;

public abstract class JobEngine {
    public abstract String submit(Job job);

    public abstract void stop(String engineJobId);

    public abstract void delete(String engineJobId);

    public abstract JobStatus getStatus(String engineJobId);

    public abstract boolean exist(String engineJobId);
}
