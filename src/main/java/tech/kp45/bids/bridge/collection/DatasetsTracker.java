package tech.kp45.bids.bridge.collection;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.collection.dataset.BidsDatasetService;

@Slf4j
@Component
public class DatasetsTracker {
    @Autowired
    private BidsDatasetService bidsDatasetService;
    @Autowired
    private BidsCollectionConfig bidsCollectionConfig;

    @Scheduled(cron = "0 0 0 * * MON")
    public void trackDatasets() {
        List<CollectorConfig> configs = bidsCollectionConfig.getCollectors();
        for (CollectorConfig config : configs) {
            try {
                bidsDatasetService.startTracking(config.getProvider());
            } catch (Exception e) {
                log.error("Error while tracking datasets for provider {} of source", config.getProvider(),
                        config.getSource(), e.getMessage());
                continue;
            }
        }
    }

}
