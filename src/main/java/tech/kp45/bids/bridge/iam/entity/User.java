package tech.kp45.bids.bridge.iam.entity;

import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("iam_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String uid;

    @TableField("username")
    private String username;

    private String email;

    private boolean emailVerified = false;

    private String avatarLink;

    @TableField("password")
    private String password;

    @TableField("enabled")
    private boolean enabled = true;

    private List<Role> roles;
}
