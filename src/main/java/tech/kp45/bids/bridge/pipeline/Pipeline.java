package tech.kp45.bids.bridge.pipeline;

import java.util.Date;

import lombok.Data;

@Data
public class Pipeline {
    private Integer id;
    private String name;
    private String version;
    private Integer workflowId;
    private Date createdAt;
    private Date updatedAt;
    private boolean deleted;
    private Date deletedAt;
}
