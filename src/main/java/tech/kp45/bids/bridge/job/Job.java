package tech.kp45.bids.bridge.job;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("job")
public class Job {
    @TableId
    private Integer id;
    @TableField("name")
    private String name;
    @TableField("group")
    private String group;
    @TableField("status")
    private String status = JobStatus.CREATED.name();
    @TableField("created_by")
    private String createdBy;
    @TableField("pipeline_id")
    private Integer pipelineId;
    @TableField("dataset_id")
    private Integer datasetId;
    private Integer filterId;
    @TableField("engine_job_id")
    private String engineJobId;
    @TableField("artifact_id")
    private Integer artifactId;
    @TableField("created_at")
    private Date createdAt;
    @TableField("updated_at")
    private Date updatedAt;
    @TableField("deleted")
    private boolean deleted = false;
    @TableField("deleted_at")
    private Date deletedAt;
}
