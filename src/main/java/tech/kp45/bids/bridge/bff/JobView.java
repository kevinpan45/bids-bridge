package tech.kp45.bids.bridge.bff;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.kp45.bids.bridge.job.Job;

@Data
@EqualsAndHashCode(callSuper = true)
public class JobView extends Job {
    private String pipelineName;
    private String pipelineVersion;
    private String pipelineWorkflow;
    private String datasetName;
    private String datasetVersion;
    private String datasetDoi;
}
