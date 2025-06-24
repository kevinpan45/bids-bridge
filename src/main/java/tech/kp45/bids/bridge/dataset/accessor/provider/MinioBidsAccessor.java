package tech.kp45.bids.bridge.dataset.accessor.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.opendal.Operator;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.collection.dataset.BidsDataset;
import tech.kp45.bids.bridge.common.exception.BasicRuntimeException;
import tech.kp45.bids.bridge.dataset.accessor.BidsCheckMode;
import tech.kp45.bids.bridge.dataset.accessor.BidsStorageAccessor;
import tech.kp45.bids.bridge.storage.Storage;

@Slf4j
public class MinioBidsAccessor extends BidsStorageAccessor {

    private final Map<String, String> conf = new HashMap<>();

    public MinioBidsAccessor(Storage storage) {
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

    @Override
    public List<BidsDataset> scan() {
        List<BidsDataset> bidses = new ArrayList<>();
        List<String> paths = listBidsPath(BidsCheckMode.BIDS_FOLDER_STRUCTURE);
        for (String path : paths) {
            BidsDataset bids = initialize(path);
            bidses.add(bids);
        }
        return bidses;
    }
}
