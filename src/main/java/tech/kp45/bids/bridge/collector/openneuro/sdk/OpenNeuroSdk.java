package tech.kp45.bids.bridge.collector.openneuro.sdk;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.http.HttpRequest;
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

    public static void main(String[] args) throws IOException {
        new OpenNeuroSdk().list(0, 0);
    }
}
