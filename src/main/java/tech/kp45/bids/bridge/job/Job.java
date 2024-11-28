package tech.kp45.bids.bridge.job;

import java.util.Date;

import lombok.Data;

@Data
public class Job {
    private Integer id;
    private String name;
    private String group;
    private String status;
    private String createdBy;
    private Integer pipelineId;
    private Integer datasetId;
    private Integer filterId;
    private Integer artifactId;
    private Date createdAt;
    private Date updatedAt;
    private boolean deleted;
    private Date deletedAt;
}
