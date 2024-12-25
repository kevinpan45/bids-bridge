package tech.kp45.bids.bridge.job.scheduler;

import tech.kp45.bids.bridge.job.Job;
import tech.kp45.bids.bridge.job.JobStatus;

public interface JobEngine {
    public String submit(Job job);

    public void stop(String engineJobId);

    public void delete(String engineJobId);

    public JobStatus getStatus(String engineJobId);

    public boolean exist(String engineJobId);
}
