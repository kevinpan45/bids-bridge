package tech.kp45.bids.bridge.dataset.storage.provider;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.opendal.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.collector.OpenNeuroCollector;
import tech.kp45.bids.bridge.common.exception.BasicRuntimeException;
import tech.kp45.bids.bridge.dataset.storage.BidsStorageService;

@Slf4j
@Component
public class OpenNeuroDal extends BidsStorageService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final Map<String, String> conf = new HashMap<>() {
        {
            put("region", "us-east-1");
            put("bucket", "openneuro.org");
            put("disable_config_load", "true");
            put("disable_ec2_metadata", "true");
            put("allow_anonymous", "true");
        }
    };

    @Override
    public Operator getOperator() {
        return Operator.of("s3", conf);
    }

    @Override
    public void scanBids(String path) {
        log.info("Scan bids from {}", path);
        File file;
        try {
            file = new File(ResourceUtil.getResource(path).toURI());
        } catch (IORuntimeException | URISyntaxException e) {
            log.error("File of path {} read failed.", path, e);
            throw new BasicRuntimeException("File read failed.");
        }
        List<String> lines = FileUtil.readUtf8Lines(file);
        log.info("Get {} bids from {}", lines.size(), path);
        for (String line : lines) {
            JSONObject item = JSONUtil.parseObj(line);
            kafkaTemplate.send(OpenNeuroCollector.BIDS_DATASET_SYNC_TOPIC, item.toString());
        }
    }

    public static void main(String[] args) {
        String content = ResourceUtil.readStr("openneuro/latest.json", StandardCharsets.UTF_8);
        JSONArray array = JSONUtil.parseArray(content);
        System.out.println(array.size());
    }
}
