package tech.kp45.bids.bridge.dataset.storage.provider;

import java.util.HashMap;
import java.util.Map;

import org.apache.opendal.Operator;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.common.exception.BasicRuntimeException;
import tech.kp45.bids.bridge.dataset.storage.BidsStorage;
import tech.kp45.bids.bridge.dataset.storage.BidsStorageService;

@Slf4j
public class MinioBidsStorageDal extends BidsStorageService {

    private final Map<String, String> conf = new HashMap<>();

    public MinioBidsStorageDal(BidsStorage storage) {
        conf.put("endpoint", storage.getEndpoint());
        conf.put("region", storage.getRegion());
        conf.put("bucket", storage.getBucket());
        if (StringUtils.hasText(storage.getPrefix())) {
            conf.put("root", storage.getPrefix());
        }
        String accessKey = storage.getAccessKey();
        String secretKey = storage.getSecretKey();
        if (StringUtils.hasText(secretKey) && StringUtils.hasText(secretKey)) {
            conf.put("access_key_id", accessKey);
            conf.put("secret_access_key", secretKey);
        } else {
            log.error("Access key or secret key is not set");
            throw new BasicRuntimeException("Access key or secret key is not set");
        }
    }

    @Override
    public Operator getOperator() {
        return Operator.of("s3", conf);
    }
}
