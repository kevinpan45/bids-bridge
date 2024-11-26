package tech.kp45.bids.bridge.collector.openneuro.sdk;

import java.util.Date;

import lombok.Data;

@Data
public class OpenNeuroDataset {
    private String datasetId;
    private String version;
    private String name;
    private String title;
    private String accessionNumber;
    private Date createdAt;
    private Date updatedAt;
}
