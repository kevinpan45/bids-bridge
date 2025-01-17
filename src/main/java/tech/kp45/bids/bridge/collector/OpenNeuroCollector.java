package tech.kp45.bids.bridge.collector;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.dataset.storage.BidsDataset;

@Slf4j
@Component
public class OpenNeuroCollector {
    public static final String BIDS_DATASET_SYNC_TOPIC = "bids-collection-dataset-openneuro";
    private static final String BIDS_DATASET_TRACKING_PREFIX = "bids:dataset:openneuro:tracking:";
    public static final String BIDS_DATASET_CACHE_PREFIX = "bids:openneuro:datasets:";
    public static final String BIDS_OPENNEURO_DATASET_ARCHTYPE_PATH = "openneuro/latest.txt";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @KafkaListener(topics = BIDS_DATASET_SYNC_TOPIC, groupId = "bids-bridge")
    public void onMessage(String content) {
        JSONObject datasetJson = new JSONObject(content);
        BidsDataset dataset = convert(datasetJson);
        String trackingKey = BIDS_DATASET_TRACKING_PREFIX + dataset.getUid();
        JSONObject inCache;
        boolean outOfDate = false;
        if (redisTemplate.hasKey(trackingKey)) {
            inCache = JSONUtil.parseObj(redisTemplate.opsForValue().get(trackingKey));
            String cachedVersion = inCache.getJSONObject("node").getJSONObject("latestSnapshot").getStr("tag");
            if (!dataset.getVersion().equals(cachedVersion)) {
                outOfDate = true;
            }
        } else {
            outOfDate = true;
            inCache = datasetJson;
        }

        if (outOfDate) {
            redisTemplate.opsForValue().set(trackingKey, inCache.toString(), 1, TimeUnit.DAYS);
            log.info("Dataset {} is updated to {}", dataset.getUid(), dataset.getVersion());
        }
    }

    public static BidsDataset convert(JSONObject gqlObject) {
        BidsDataset dataset = new BidsDataset();
        JSONObject node = gqlObject.getJSONObject("node");
        JSONObject latestSnapshot = node.getJSONObject("latestSnapshot");
        String accessionNumber = gqlObject.getStr("id");
        String name = latestSnapshot.getJSONObject("description").getStr("Name");
        String modality = latestSnapshot.getJSONObject("summary").getJSONArray("modalities").getStr(0);
        int participants = latestSnapshot.getJSONObject("summary").getJSONArray("subjects").size();
        String latestVersion = latestSnapshot.getStr("tag");

        dataset.setUid(accessionNumber);
        dataset.setName(name);
        dataset.setVersion(latestVersion);
        dataset.setModality(modality);
        dataset.setParticipants(participants);
        dataset.setValid(true);

        return dataset;
    }
}
