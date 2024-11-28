package tech.kp45.bids.bridge.dataset.storage;

import lombok.Data;

@Data
public class BidsStorage {
    private int id;
    private int datasetId;
    private String path;
    private String packingPath;
}
