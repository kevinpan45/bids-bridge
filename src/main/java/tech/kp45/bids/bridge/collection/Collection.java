package tech.kp45.bids.bridge.collection;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("dataset_collection")
public class Collection {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField("description")
    private String description;
    @TableField("bids_dataset_id")
    private Integer bidsDatasetId;
    @TableField("storage_id")
    private Integer storageId;
    @TableField("storage_path")
    private String storagePath;
    @TableField("collection_execution_id")
    private String collectionExecutionId;
    /**
     * The status of the collection. Possible values are CollectionStatus names.
     * 
     * @see CollectionStatus
     */
    @TableField("status")
    private String status;
    @TableField("created_at")
    private Date createdAt;
    @TableField("updated_at")
    private Date updatedAt;
}
