package tech.kp45.bids.bridge.dataset.accessor.provider;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.opendal.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.collection.BidsDataset;
import tech.kp45.bids.bridge.common.exception.BasicRuntimeException;
import tech.kp45.bids.bridge.dataset.accessor.BidsStorageAccessor;

@Slf4j
@Component
public class OpenNeuroAccessor extends BidsStorageAccessor {

    public static final String OPENNEURO_BIDS_TRACK_TOPIC = "openneuro.bids.track";
    private static final String BIDS_DATASET_TRACKING_PREFIX = "bids:dataset:openneuro:tracking:";
    public static final String BIDS_DATASET_TACKING_KEY_PATTERN = BIDS_DATASET_TRACKING_PREFIX + "*:metadata";
    public static final String BIDS_OPENNEURO_DATASET_ARCHTYPE_PATH = "openneuro/latest.txt";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private final Map<String, String> conf = Map.of(
            "region", "us-east-1",
            "bucket", "openneuro.org",
            "disable_config_load", "true",
            "disable_ec2_metadata", "true",
            "allow_anonymous", "true");

    @Override
    public Operator getOperator() {
        return Operator.of("s3", conf);
    }

    private List<String> loadCached(String path) {
        String content;
        try {
            content = ResourceUtil.readStr(path, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Load BIDS from {} failed", path, e);
            throw new BasicRuntimeException("Load BIDS failed");
        }
        List<String> lines = content.lines().toList();
        log.info("Get {} bids from {}", lines.size(), path);
        return lines;
    }

    @Override
    public List<BidsDataset> scan() {
        List<BidsDataset> bidses = new ArrayList<>();
        String path = BIDS_OPENNEURO_DATASET_ARCHTYPE_PATH;
        log.info("Scan bids from {}", path);
        List<String> lines = loadCached(path);
        for (String line : lines) {
            JSONObject datasetJson = JSONUtil.parseObj(line);
            BidsDataset bids = convert(datasetJson);
            String trackingMetaKey = getTrackingKey(bids.getDoi(), bids.getVersion());
            redisTemplate.opsForValue().set(trackingMetaKey, line, 1, TimeUnit.DAYS);
            bidses.add(bids);
        }
        return bidses;
    }

    /**
     * 
     * @return all tracking keys for getting dataset metadata
     */
    public Set<String> getTrackingKeys() {
        return redisTemplate.keys(BIDS_DATASET_TACKING_KEY_PATTERN);
    }

    public String getTrackingKey(String doi, String version) {
        return BIDS_DATASET_TRACKING_PREFIX + doi + ":" + version + ":metadata";
    }

    public BidsDataset getTackingDataset(String key) {
        String meta = redisTemplate.opsForValue().get(key);
        return convert(JSONUtil.parseObj(meta));
    }

    private BidsDataset convert(JSONObject gqlObject) {
        BidsDataset dataset = new BidsDataset();
        JSONObject node = gqlObject.getJSONObject("node");
        JSONObject latestSnapshot = node.getJSONObject("latestSnapshot");
        String accessionNumber = gqlObject.getStr("id");
        String name = latestSnapshot.getJSONObject("description").getStr("Name");
        String modality = latestSnapshot.getJSONObject("summary").getJSONArray("modalities").getStr(0);
        int participants = latestSnapshot.getJSONObject("summary").getJSONArray("subjects").size();
        String latestVersion = latestSnapshot.getStr("tag");
        long size = latestSnapshot.getLong("size");

        dataset.setDoi(accessionNumber)
                .setName(name)
                .setVersion(latestVersion)
                .setModality(modality)
                .setParticipants(participants)
                .setSize(size)
                .setStoragePath(accessionNumber + "/")
                .setValid(true);

        return dataset;
    }

}
