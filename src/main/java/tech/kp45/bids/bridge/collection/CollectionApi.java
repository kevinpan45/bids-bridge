package tech.kp45.bids.bridge.collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CollectionApi {
    @Autowired
    private BidsDatasetService bidsDatasetService;

    @PostMapping("/api/collections/datasets/tasks")
    public void startTrackingTask(@RequestParam String provider) {
        bidsDatasetService.startTracking(provider);
    }
}
