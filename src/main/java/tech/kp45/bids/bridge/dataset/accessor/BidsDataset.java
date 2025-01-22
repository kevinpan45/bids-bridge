package tech.kp45.bids.bridge.dataset.accessor;

import lombok.Data;
import lombok.experimental.Accessors;
import tech.kp45.bids.bridge.dataset.Dataset;

/**
 * Each version of the dataset database record
 */
@Data
@Accessors(chain = true)
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
    private long size;
    /**
     * Valid by BIDS Validator or other validation program
     */
    private boolean valid;
    /**
     * Contains derivatives
     */
    private boolean derived;

    public Dataset toDataset() {
        Dataset dataset = new Dataset();
        dataset.setName(name);
        dataset.setVersion(version);
        dataset.setStoragePath(storagePath);
        return dataset;
    }
}
