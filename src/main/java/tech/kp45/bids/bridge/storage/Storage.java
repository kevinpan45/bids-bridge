package tech.kp45.bids.bridge.storage;

import java.util.Map;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("storage")
public class Storage {
    @TableId
    private Integer id;
    @TableField("`name`")
    private String name;
    @TableField("provider")
    private String provider;
    @TableField("endpoint")
    private String endpoint;
    @TableField("access_key")
    private String accessKey;
    @TableField("secret_key")
    private String secretKey;
    @TableField("bucket")
    private String bucket;
    @TableField("prefix")
    private String prefix;
    @TableField("region")
    private String region;
    @TableField("externals")
    private Map<String, String> externals;
}
