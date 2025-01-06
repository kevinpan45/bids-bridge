package tech.kp45.bids.bridge.collector;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import tech.kp45.bids.bridge.dataset.storage.provider.OpenNeuroDal;

@Configuration
public class OpenNeuroCollectionTrigger {

    private static final String OPENNEURO_SYNC_TASK_LOCK = "OPENNEURO_SYNC_TASK_LOCK";

    @Autowired
    private OpenNeuroDal openNeuroDal;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Scheduled(cron = "${bids.collector.openneuro.sync.cron}")
    public void trigger() {
        boolean acquired = false;
        try {
            acquired = redisTemplate.opsForValue().setIfAbsent(OPENNEURO_SYNC_TASK_LOCK, "locked", 30,
                    TimeUnit.SECONDS);
            if (acquired) {
                openNeuroDal.scanBids("openneuro/latest.txt");
            }
        } finally {
            if (acquired) {
                redisTemplate.delete(OPENNEURO_SYNC_TASK_LOCK);
            }
        }
    }
}
