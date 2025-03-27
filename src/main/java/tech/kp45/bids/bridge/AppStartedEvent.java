package tech.kp45.bids.bridge;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.dataset.accessor.BidsDataset;
import tech.kp45.bids.bridge.dataset.accessor.provider.OpenNeuroAccessor;

@Slf4j
@Component
public class AppStartedEvent implements ApplicationListener<ApplicationReadyEvent> {
    @Autowired
    private OpenNeuroAccessor openNeuroAccessor;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // Cache OpenNeuro
        openneuroBidsInit();
    }

    private void openneuroBidsInit() {
        Set<String> keys = openNeuroAccessor.getTrackingKeys();
        if (keys.isEmpty()) {
            List<BidsDataset> datasets = openNeuroAccessor.scan();
            log.info("Load {} datasets from local storage", datasets.size());
        }
    }

}
