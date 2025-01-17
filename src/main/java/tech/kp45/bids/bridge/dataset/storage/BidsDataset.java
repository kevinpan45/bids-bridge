package tech.kp45.bids.bridge.dataset.storage;

import lombok.Data;

/**
 * Each version of the dataset database record
 */
@Data
public class BidsDataset {
    /**
     * The unique identifier of the dataset for platform management
     */
    private String uid;
    /**
     * The relative path of the dataset in the object storage bucket and root path
     */
    private String storagePath;
    private String name;
    private String modality;
    private String dataType;
    private String version;
    private String bidsVersion;
    private int participants;
    /**
     * Valid by BIDS Validator or other validation program
     */
    private boolean valid;
    /**
     * Contains derivatives
     */
    private boolean derived;
}
