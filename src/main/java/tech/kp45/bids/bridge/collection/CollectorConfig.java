package tech.kp45.bids.bridge.collection;

import lombok.Data;

@Data
public class CollectorConfig {
    private String provider;
    private String source;
    private SyncConfig sync;
    private TrackConfig track;
    private String description;

}

@Data
class SyncConfig {
    private String enabled;
    private String cron;
}

@Data
class TrackConfig {
    private String enabled;
    private String cron;
}