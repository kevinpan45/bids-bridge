package tech.kp45.bids.bridge.collector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OpenNeuroCollector {
    public static final String BIDS_DATASET_SYNC_TOPIC = "bids-collection-dataset-openneuro";
    private static final String BIDS_DATASET_TRACKING_PREFIX = "bids:dataset:openneuro:tracking:";
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @KafkaListener(topics = BIDS_DATASET_SYNC_TOPIC, groupId = "bids-bridge")
    public void onMessage(String content) {
        JSONObject datasetJson = new JSONObject(content);
        String accessionNumber = datasetJson.getStr("id");
        String latestVersion = datasetJson.getJSONObject("node").getJSONObject("latestSnapshot").getStr("tag");
        String trackingKey = BIDS_DATASET_TRACKING_PREFIX + accessionNumber;
        JSONObject inCache;
        boolean outOfDate = false;
        if (redisTemplate.hasKey(trackingKey)) {
            inCache = JSONUtil.parseObj(redisTemplate.opsForValue().get(trackingKey));
            if (!latestVersion.equals(inCache.getJSONObject("node").getJSONObject("latestSnapshot").getStr("tag"))) {
                outOfDate = true;
            }
        } else {
            outOfDate = true;
            inCache = datasetJson;
        }

        if (outOfDate) {
            redisTemplate.opsForValue().set(trackingKey, inCache.toString());
            log.info("Dataset {} is updated.", accessionNumber);
        }
    }
}
