package tech.kp45.bids.bridge;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.dataset.Dataset;
import tech.kp45.bids.bridge.dataset.DatasetService;
import tech.kp45.bids.bridge.job.Job;
import tech.kp45.bids.bridge.job.JobService;
import tech.kp45.bids.bridge.pipeline.Pipeline;
import tech.kp45.bids.bridge.pipeline.PipelineService;
import tech.kp45.bids.bridge.storage.Storage;
import tech.kp45.bids.bridge.storage.StorageService;

@Slf4j
@Component
public class AppStartedEvent implements ApplicationListener<ApplicationReadyEvent> {
    @Autowired
    private PipelineService pipelineService;
    @Autowired
    private DatasetService datasetService;
    @Autowired
    private JobService jobService;
    @Autowired
    private StorageService storageService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        List<Pipeline> pipelines = pipelineService.list();
        log.info("Pipeline count: {}", pipelines.size());

        List<Dataset> datasets = datasetService.list();
        log.info("Dataset count: {}", datasets.size());

        List<Job> jobs = jobService.list();
        log.info("Job count: {}", jobs.size());

        List<Storage> storages = storageService.list();
        log.info("Storage count: {}", storages.size());
    }

}
