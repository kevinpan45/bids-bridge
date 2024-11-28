package tech.kp45.bids.bridge.dataset.storage;

import lombok.Data;

@Data
public class BidsStorage {
    private Integer id;
    private Integer datasetId;
    private String path;
    private String packingPath;
}
