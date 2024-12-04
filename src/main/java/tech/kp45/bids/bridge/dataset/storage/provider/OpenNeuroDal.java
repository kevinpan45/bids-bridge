package tech.kp45.bids.bridge.dataset.storage.provider;

import java.util.HashMap;
import java.util.Map;

import org.apache.opendal.Operator;

import tech.kp45.bids.bridge.dataset.storage.BidsStorageService;

public class OpenNeuroDal extends BidsStorageService {

    private final Map<String, String> conf = new HashMap<>() {
        {
            put("region", "us-east-1");
            put("bucket", "openneuro.org");
            put("disable_config_load", "true");
            put("disable_ec2_metadata", "true");
            put("allow_anonymous", "true");
        }
    };

    @Override
    public Operator getOperator() {
        return Operator.of("s3", conf);
    }
}
