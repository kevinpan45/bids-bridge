package tech.kp45.bids.bridge.dataset;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.experimental.Accessors;
import tech.kp45.bids.bridge.collection.OpenNeuroCollectionTracker;
import tech.kp45.bids.bridge.collection.dataset.BidsDataset;

@Data
@Accessors(chain = true)
@TableName("dataset")
public class Dataset {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField("`name`")
    private String name;
    @TableField("version")
    private String version;
    @TableField("doi")
    private String doi;
    @TableField("storage_id")
    private Integer storageId;
    @TableField("storage_path")
    private String storagePath;
    @TableField("provider")
    private String provider;
    @TableField("description")
    private String description;
    /**
     * Collect from which BIDS dataset in the platform tracking system.
     * @see BidsDataset
     */
    @TableField("collect_from")
    private Integer collectFrom;
    /**
     * The event that triggered the collection of this dataset.
     * @see OpenNeuroCollectionTracker
     */
    @TableField("collect_event")
    private String collectEvent;
    @TableField("created_at")
    private Date createdAt;
    @TableField("updated_at")
    private Date updatedAt;
    @TableField("deleted")
    private boolean deleted;
    @TableField("deleted_at")
    private Date deletedAt;
}
