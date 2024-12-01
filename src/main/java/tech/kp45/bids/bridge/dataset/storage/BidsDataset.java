package tech.kp45.bids.bridge.dataset.storage;

import lombok.Data;

@Data
public class BidsDataset {
    private String uid;
    private String storagePath;
    private String name;
    private String modality;
    private String dataType;
    private String version;
    private String bidsVersion;
    private boolean valid;
    private boolean derived;
}
