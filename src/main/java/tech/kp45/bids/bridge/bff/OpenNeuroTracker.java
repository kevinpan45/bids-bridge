package tech.kp45.bids.bridge.bff;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.dataset.accessor.BidsDataset;
import tech.kp45.bids.bridge.dataset.accessor.provider.OpenNeuroAccessor;

@Slf4j
@Configuration
public class OpenNeuroTracker {

    private static final String OPENNEURO_SYNC_TASK_LOCK = "OPENNEURO_SYNC_TASK_LOCK";

    @Autowired
    private OpenNeuroAccessor accessor;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Value("${bids.collector.openneuro.sync.enabled}")
    private boolean enabled;

    @Scheduled(cron = "${bids.collector.openneuro.sync.cron}")
    public void trigger() {
        if (!enabled) {
            return;
        }
        boolean acquired = false;
        try {
            acquired = redisTemplate.opsForValue().setIfAbsent(OPENNEURO_SYNC_TASK_LOCK, "locked", 30,
                    TimeUnit.SECONDS);
            if (acquired) {
                List<BidsDataset> bidses = accessor.scan();
                for (BidsDataset bids : bidses) {
                    kafkaTemplate.send(OpenNeuroAccessor.OPENNEURO_BIDS_TRACK_TOPIC, JSONUtil.toJsonStr(bids));
                }
            }
        } finally {
            if (acquired) {
                redisTemplate.delete(OPENNEURO_SYNC_TASK_LOCK);
            }
        }
    }

    @KafkaListener(topics = OpenNeuroAccessor.OPENNEURO_BIDS_TRACK_TOPIC, groupId = "bids-openneuro-tracker")
    public void listenOnOpenNeuroTrackEvent(String message) {
        BidsDataset bids = JSONUtil.toBean(message, BidsDataset.class);
        List<String> files = new ArrayList<>();
        try {
            accessor.scanFiles(bids.getStoragePath(), files);
        } catch (Exception e) {
            log.error("Failed to scan files for BIDS dataset {} : {}", bids.getStoragePath(), e.getMessage());
            return;
        }
        String bidsFilesKey = "bids:dataset:openneuro:tracking:" + bids.getUid() + ":" + bids.getVersion() + ":files:";
        files.stream().forEach(file -> {
            String filename = StringUtils.getFilename(file);
            String fileKey = bidsFilesKey + filename;
            if (!redisTemplate.hasKey(fileKey)) {
                redisTemplate.opsForValue().set(fileKey, file);
            }
        });
    }
}
