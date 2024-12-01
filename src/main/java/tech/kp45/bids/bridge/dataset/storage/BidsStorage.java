package tech.kp45.bids.bridge.dataset.storage;

import lombok.Data;

@Data
public class BidsStorage {
    private Integer id;
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String prefix;
    private String region;
}
