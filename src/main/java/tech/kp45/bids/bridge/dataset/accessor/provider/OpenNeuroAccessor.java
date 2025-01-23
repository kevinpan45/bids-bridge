package tech.kp45.bids.bridge.dataset.accessor.provider;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.opendal.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.common.exception.BasicRuntimeException;
import tech.kp45.bids.bridge.dataset.Dataset;
import tech.kp45.bids.bridge.dataset.DatasetService;
import tech.kp45.bids.bridge.dataset.accessor.BidsDataset;
import tech.kp45.bids.bridge.dataset.accessor.BidsStorageAccessor;

@Slf4j
@Component
public class OpenNeuroAccessor extends BidsStorageAccessor {

    public static final String BIDS_DATASET_SYNC_TOPIC = "bids-collection-dataset-openneuro";
    private static final String BIDS_DATASET_TRACKING_PREFIX = "bids:dataset:openneuro:tracking:";
    public static final String BIDS_DATASET_CACHE_PREFIX = "bids:openneuro:datasets:";
    public static final String BIDS_OPENNEURO_DATASET_ARCHTYPE_PATH = "openneuro/latest.txt";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private DatasetService datasetService;

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
        File file;
        try {
            file = new File(ResourceUtil.getResource(path).toURI());
        } catch (IORuntimeException | URISyntaxException e) {
            log.error("Load BIDS from {} failed", path, e);
            throw new BasicRuntimeException("Load BIDS failed");
        }
        List<String> lines = FileUtil.readUtf8Lines(file);
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
            String trackingMetaKey = BIDS_DATASET_TRACKING_PREFIX + bids.getUid() + ":" + bids.getVersion()
                    + ":metadata";
            redisTemplate.opsForValue().set(trackingMetaKey, line, 1, TimeUnit.DAYS);
            bidses.add(bids);
        }
        return bidses;
    }

    public List<Dataset> load() {
        List<Dataset> datasets = new ArrayList<>();
        String path = BIDS_OPENNEURO_DATASET_ARCHTYPE_PATH;
        List<String> lines = loadCached(path);
        for (String line : lines) {
            JSONObject item = JSONUtil.parseObj(line);
            BidsDataset bids = convert(item);
            bids.setValid(true);
            redisTemplate.opsForValue().set(BIDS_DATASET_CACHE_PREFIX + bids.getUid(),
                    JSONUtil.toJsonStr(bids), 1, TimeUnit.DAYS);
            Dataset dataset = bids.toDataset();
            datasetService.create(dataset);
            datasets.add(dataset);
        }
        return datasets;
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

        dataset.setUid(accessionNumber)
                .setName(name)
                .setVersion(latestVersion)
                .setModality(modality)
                .setParticipants(participants)
                .setSize(size)
                .setValid(true);

        return dataset;
    }

}
