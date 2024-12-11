package tech.kp45.bids.bridge.dataset.storage;

import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BidsStorage {
    private Integer id;
    private String name;
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String prefix;
    private String region;
    private Map<String, String> externals;
}
