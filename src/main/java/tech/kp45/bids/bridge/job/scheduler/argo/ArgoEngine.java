package tech.kp45.bids.bridge.job.scheduler.argo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tech.kp45.bids.bridge.common.exception.BasicRuntimeException;
import tech.kp45.bids.bridge.dataset.Dataset;
import tech.kp45.bids.bridge.dataset.DatasetService;
import tech.kp45.bids.bridge.job.Job;
import tech.kp45.bids.bridge.job.JobStatus;
import tech.kp45.bids.bridge.job.scheduler.JobEngine;
import tech.kp45.bids.bridge.pipeline.Pipeline;
import tech.kp45.bids.bridge.pipeline.PipelineService;
import tech.kp45.bids.bridge.storage.Storage;

@Component
public class ArgoEngine extends JobEngine {

    @Autowired
    private PipelineService pipelineService;
    @Autowired
    private DatasetService datasetService;
    @Autowired
    private ArgoProperties argoProperties;

    @Override
    public String submit(Job job) {
        Pipeline pipeline = pipelineService.get(job.getPipelineId());
        String workflow = pipeline.getWorkflow();
        ArgoSdk argoSdk = new ArgoSdk(argoProperties);
        boolean workflowExist = argoSdk.workflowTemplateExist(workflow);
        if (!workflowExist) {
            throw new BasicRuntimeException("Workflow template " + workflow + " not deployed.");
        }

        Dataset dataset = datasetService.get(job.getDatasetId());

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("dataset", dataset.getStoragePath());
        return argoSdk.submit(workflow, parameters);
    }

    @Override
    public void stop(String engineJobId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'stop'");
    }

    @Override
    public void delete(String engineJobId) {
        ArgoSdk argoSdk = new ArgoSdk(argoProperties);
        argoSdk.deleteWorkflow(engineJobId);
    }

    @Override
    public JobStatus getStatus(String engineJobId) {
        ArgoSdk argoSdk = new ArgoSdk(argoProperties);
        String statusInEngine = argoSdk.getWorkflowStatus(engineJobId);
        switch (statusInEngine) {
            case "Running":
                return JobStatus.RUNNING;
            case "Succeeded":
                return JobStatus.FINISHED;
            case "Failed":
                return JobStatus.FAILED;
            case "Error":
                return JobStatus.FAILED;
            case "Pending":
                return JobStatus.RUNNING;
            case "Unknown":
                return JobStatus.FAILED;
            default:
                throw new BasicRuntimeException("Unknown status: " + statusInEngine);
        }
    }

    @Override
    public boolean exist(String engineJobId) {
        return new ArgoSdk(argoProperties).listWorkflow().contains(engineJobId);
    }

    @Override
    public boolean deployed(String enginePipelineId) {
        return new ArgoSdk(argoProperties).workflowTemplateExist(enginePipelineId);
    }

    @Override
    public List<String> getPipelines() {
        return new ArgoSdk(argoProperties).listWorkflowTemplate();
    }

    @Override
    public void setupStorage(Storage storage) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setupStorage'");
    }

    @Override
    public boolean storageAvailable() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'storageAvailable'");
    }

}
