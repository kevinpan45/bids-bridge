package tech.kp45.bids.bridge.job.service;

import java.util.List;

import tech.kp45.bids.bridge.job.Job;

public abstract class JobService {
    abstract Job create(Integer pipelineId, Integer datasetId);

    abstract Job create(Integer pipelineId, Integer datasetId, List<String> fileRegexes);

    abstract String schedule(Integer jobId);

    abstract void delete(Integer jobId);
}
