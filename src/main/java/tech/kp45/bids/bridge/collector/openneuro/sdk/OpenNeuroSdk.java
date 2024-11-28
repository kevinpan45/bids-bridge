package tech.kp45.bids.bridge.collector.openneuro.sdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.opendal.Entry;
import org.apache.opendal.Operator;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenNeuroSdk {

    public List<OpenNeuroDataset> list(int page, int size) {
        List<OpenNeuroDataset> datasets = new ArrayList<>();
        Operator op = new OpenNeuroSdk().getOperator();
        List<Entry> list = op.list("");
        if (!list.isEmpty()) {
            list.forEach(item -> {
                OpenNeuroDataset dataset = new OpenNeuroDataset();
                dataset.setAccessionNumber(item.path);
                datasets.add(dataset);
            });
        }
        return datasets;
    }

    public OpenNeuroDataset get(String datasetId) {
        Operator op = new OpenNeuroSdk().getOperator();
        byte[] desc = op.read(datasetId + "/dataset_description.json");
        JSONObject bidsDescription = JSONUtil.parseObj(desc);
        log.info(bidsDescription.getStr("Name"));
        OpenNeuroDataset dataset = new OpenNeuroDataset();
        dataset.setName(bidsDescription.getStr("Name"));
        dataset.setAccessionNumber(datasetId);
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
}
