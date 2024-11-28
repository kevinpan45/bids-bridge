package tech.kp45.bids.bridge.job;

import java.util.Date;

import lombok.Data;

@Data
public class Job {
    private int id;
    private String name;
    private String group;
    private String status;
    private String createdBy;
    private int pipelineId;
    private int datasetId;
    private int fileFilterId;
    private int artifactId;
    private Date createdAt;
    private Date updatedAt;
    private boolean deleted;
    private Date deletedAt;
}
