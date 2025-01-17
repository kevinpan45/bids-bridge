package tech.kp45.bids.bridge.dataset.storage.provider;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.opendal.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.collector.OpenNeuroCollector;
import tech.kp45.bids.bridge.common.exception.BasicRuntimeException;
import tech.kp45.bids.bridge.dataset.storage.BidsDataset;
import tech.kp45.bids.bridge.dataset.storage.BidsStorageService;

@Slf4j
@Component
public class OpenNeuroDal extends BidsStorageService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

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

    @Override
    public void scanBids(String path) {
        log.info("Scan bids from {}", path);
        List<String> lines = loadBids(path);
        for (String line : lines) {
            JSONObject item = JSONUtil.parseObj(line);
            kafkaTemplate.send(OpenNeuroCollector.BIDS_DATASET_SYNC_TOPIC, item.toString());
        }
    }

    public List<BidsDataset> load(String path) {
        List<BidsDataset> datasets = new ArrayList<>();
        List<String> lines = loadBids(path);
        for (String line : lines) {
            JSONObject item = JSONUtil.parseObj(line);
            BidsDataset dataset = OpenNeuroCollector.convert(item);
            dataset.setValid(true);
            datasets.add(dataset);
            redisTemplate.opsForValue().set(OpenNeuroCollector.BIDS_DATASET_CACHE_PREFIX + dataset.getUid(),
                    JSONUtil.toJsonStr(dataset));
        }
        return datasets;
    }

    private List<String> loadBids(String path) {
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
}
