package tech.kp45.bids.bridge.job.scheduler.argo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "job.engine.argo")
public class ArgoProperties {
    private String serverUrl;
    private String namespace;
    private String token;
}
