package tech.kp45.bids.bridge.job.scheduler.argo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Kubernetes streamingConnectionIdleTimeout mechanism will stop the port
 * forwarding
 * if the connection is idle for a certain period. This class is used to keep
 * Argo Workflow port forwarding alive
 * by periodically checking the connection to the Argo Engine.
 */
@Slf4j
@Component
public class ArgoEngineAliveKeeper {

    @Autowired
    private ArgoProperties argoProperties;

    @Scheduled(cron = "0 */5 * * * *")
    public void checkArgoEngineAlive() {
        ArgoSdk argoSdk = new ArgoSdk(argoProperties);
        if (!argoSdk.test()) {
            log.warn("Argo Engine is not alive. Please check the connection.");
        }
    }
}
