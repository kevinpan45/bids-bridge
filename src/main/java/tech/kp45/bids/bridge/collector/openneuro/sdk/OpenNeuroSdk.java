package tech.kp45.bids.bridge.collector.openneuro.sdk;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.opendal.Entry;
import org.apache.opendal.Metadata;
import org.apache.opendal.Operator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenNeuroSdk {
    private String apiPath = "https://openneuro.org/crn/graphql";

    public List<OpenNeuroDataset> list(int page, int size) throws IOException {
        Operator op = new OpenNeuroSdk().getOperator();
        List<Entry> list = op.list("");
        if (!list.isEmpty()) {
            list.forEach(item -> {
                OpenNeuroDataset dataset = new OpenNeuroDataset();
                dataset.setAccessionNumber(item.path);
            });
        }
        return null;
    }

    public OpenNeuroDataset get(String datasetId) throws IOException {
        Operator op = new OpenNeuroSdk().getOperator();
        byte[] desc = op.read(datasetId + "/dataset_description.json");
        JSONObject bidsDescription = JSONUtil.parseObj(desc);
        log.info(bidsDescription.getStr("Name"));
        OpenNeuroDataset dataset = new OpenNeuroDataset();
        return dataset;
    }

    private Operator getOperator() {
        final Map<String, String> conf = new HashMap<>();
        conf.put("region", "us-east-1");
        conf.put("bucket", "openneuro.org");
        conf.put("disable_config_load", "true");
        conf.put("disable_ec2_metadata", "true");
        conf.put("allow_anonymous", "true");
        Operator op = Operator.of("s3", conf);
        return op;
    }

    public static void main(String[] args) throws IOException {
        OpenNeuroSdk sdk = new OpenNeuroSdk();
        sdk.get("ds005619");
    }
}
