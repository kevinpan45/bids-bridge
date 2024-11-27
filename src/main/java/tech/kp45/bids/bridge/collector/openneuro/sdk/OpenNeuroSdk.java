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
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenNeuroSdk {
    private String apiPath = "https://openneuro.org/crn/graphql";

    public List<OpenNeuroDataset> list(int page, int size) throws IOException {
        ClassPathResource resource = new ClassPathResource("./collector/openneuro/graphql-list-query.json");
        String queryString = resource.readStr(StandardCharsets.UTF_8);
        String response = query(queryString);
        log.info(response);
        return null;
    }

    public OpenNeuroDataset get(String datasetId) throws IOException {
        ClassPathResource resource = new ClassPathResource("./collector/openneuro/graphql-get-query.json");
        String queryString = resource.readStr(StandardCharsets.UTF_8);
        queryString = queryString.replace("DATASET_ACCESSION_NUMBER", datasetId);
        String response = query(queryString);
        log.info(response);
        OpenNeuroDataset dataset = new OpenNeuroDataset();
        return dataset;
    }

    private String query(String query) throws IOException {
        return HttpRequest.post(apiPath).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).body(query)
                .execute().body();

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
        Operator op = new OpenNeuroSdk().getOperator();
        List<Entry> list = op.list("ds005619/");
        if (!list.isEmpty()) {
            list.forEach(item -> {
                System.out.println(item);
            });
        }
    }
}
