package tech.kp45.bids.bridge.dataset.accessor.provider;

import java.util.List;
import java.util.Map;

import org.apache.opendal.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.collection.dataset.BidsDataset;
import tech.kp45.bids.bridge.collection.dataset.BidsDatasetService;
import tech.kp45.bids.bridge.dataset.DatasetProvider;
import tech.kp45.bids.bridge.dataset.accessor.BidsStorageAccessor;

@Slf4j
@Component
public class OpenNeuroAccessor extends BidsStorageAccessor {

    @Autowired
    private BidsDatasetService bidsDatasetService;

    private final Map<String, String> conf = Map.of(
            "region", "us-east-1",
            "bucket", "openneuro.org",
            "disable_config_load", "true",
            "disable_ec2_metadata", "true",
            "allow_anonymous", "true");

    @Override
    public Operator getOperator() {
        return Operator.of("s3", conf);
    }

    @Override
    public List<BidsDataset> scan() {
        // Read from database record by DatasetTracker
        return bidsDatasetService.list(DatasetProvider.OPENNEURO.getName());
    }

}
