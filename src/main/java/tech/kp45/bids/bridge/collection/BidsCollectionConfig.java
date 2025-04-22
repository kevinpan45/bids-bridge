package tech.kp45.bids.bridge.collection;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "bids")
public class BidsCollectionConfig {
    private List<CollectorConfig> collectors;

    public CollectorConfig getCollector(String provider) {
        if (StringUtils.hasText(provider)) {
            for (CollectorConfig collector : collectors) {
                if (collector.getProvider().equals(provider)) {
                    return collector;
                }
            }
        }
        return null;
    }
}
