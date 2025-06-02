package tech.kp45.bids.bridge.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    @Autowired
    private HealthEndpoint healthEndpoint;

    @GetMapping("/api/status")
    public String getApiServerStatus() {
        // Forward the status from the Spring Boot Actuator health endpoint
        return healthEndpoint.health().getStatus().getCode();
    }
}