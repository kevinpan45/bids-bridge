package tech.kp45.bids.bridge.iam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("iam_role")
public class Role {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField("code")
    private String code;
    @TableField("name")
    private String name;
}
