package tech.kp45.bids.bridge.pipeline;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("pipeline")
public class Pipeline {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField("`name`")
    private String name;
    @TableField("version")
    private String version;
    @TableField("workflow")
    private String workflow;
    @TableField("description")
    private String description;
    @TableField("created_at")
    private Date createdAt;
    @TableField("updated_at")
    private Date updatedAt;
    @TableField("deleted")
    private boolean deleted;
    @TableField("deleted_at")
    private Date deletedAt;

    public String getFullName() {
        return name + ":" + version;
    }
}
