package tech.kp45.bids.bridge.job.artifact;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("artifact")
public class Artifact {
    @TableId
    private Integer id;
    @TableField("job_id")
    private Integer jobId;
    @TableField("storage_path")
    private String storagePath;
    @TableField("created_at")
    private Date createdAt;
    @TableField("updated_at")
    private Date updatedAt;
    @TableField("deleted")
    private boolean deleted;
    @TableField("deleted_at")
    private Date deletedAt;
}
