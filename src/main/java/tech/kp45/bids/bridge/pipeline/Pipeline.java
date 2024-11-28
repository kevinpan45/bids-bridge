package tech.kp45.bids.bridge.pipeline;

import java.util.Date;

import lombok.Data;

@Data
public class Pipeline {
    private int id;
    private String name;
    private String version;
    private String dag;
    private Date createdAt;
    private Date updatedAt;
    private boolean deleted;
    private Date deletedAt;
}
