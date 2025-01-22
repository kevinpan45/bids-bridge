package tech.kp45.bids.bridge.pipeline.workflow;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("workflow")
public class Workflow {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField("`dag`")
    private String dag;
    @TableField("created_at")
    private Date createdAt;
    @TableField("updated_at")
    private Date updatedAt;
    @TableField("deleted")
    private boolean deleted;
    @TableField("deleted_at")
    private Date deletedAt;
}
