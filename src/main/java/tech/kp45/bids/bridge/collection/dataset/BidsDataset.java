package tech.kp45.bids.bridge.collection.dataset;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.experimental.Accessors;
import tech.kp45.bids.bridge.dataset.Dataset;

/**
 * Each version of the dataset database record
 */
@Data
@Accessors(chain = true)
@TableName("bids_dataset")
public class BidsDataset {
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * The unique identifier of the dataset for platform management
     */
    @TableField("doi")
    private String doi;
    /**
     * The relative path of the dataset in the object storage bucket and root path
     */
    @TableField("storage_path")
    private String storagePath;
    @TableField("collected")
    private boolean collected;
    @TableField("`name`")
    private String name;
    @TableField("modality")
    private String modality;
    @TableField("data_type")
    private String dataType;
    @TableField("version")
    private String version;
    @TableField("bids_version")
    private String bidsVersion;
    @TableField("participants")
    private int participants;
    @TableField("size")
    private long size;
    /**
     * Valid by BIDS Validator or other validation program
     */
    @TableField("valid")
    private boolean valid;
    /**
     * Contains derivatives
     */
    @TableField("derived")
    private boolean derived;

    @TableField("provider")
    private String provider;

    public Dataset toDataset() {
        Dataset dataset = new Dataset();
        dataset.setName(name);
        dataset.setVersion(version);
        dataset.setStoragePath(storagePath);
        dataset.setDoi(doi);
        return dataset;
    }
}
