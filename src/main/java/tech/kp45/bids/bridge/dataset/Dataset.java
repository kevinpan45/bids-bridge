package tech.kp45.bids.bridge.dataset;

import java.util.Date;

import lombok.Data;

@Data
public class Dataset {
    private int id;
    private String name;
    private String version;
    private Date createdAt;
    private Date updatedAt;
    private boolean deleted;
    private Date deletedAt;
}
