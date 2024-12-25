package tech.kp45.bids.bridge.job.scheduler.argo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.Method;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.common.exception.BasicRuntimeException;

@Slf4j
public class ArgoSdk {

    private String serverUrl;
    private String namespace;
    private String token;

    public ArgoSdk(String serverUrl, String namespace, String token) {
        if (!StringUtils.hasText(serverUrl) || !StringUtils.hasText(namespace)) {
            throw new BasicRuntimeException("ArgoSdk init failed, apiPath or namespace is empty");
        }
        this.serverUrl = serverUrl;
        this.namespace = namespace;
        if (StringUtils.hasText(token)) {
            this.token = token;
        } else {
            log.warn("ArgoSdk is initialized without token.");
        }
    }

    public boolean test() {
        try {
            return HttpRequest.get(serverUrl + "/api/v1/workflows/argo").execute().isOk();
        } catch (Exception e) {
            log.error("Argo Workflows server {} request error.", serverUrl, e);
            throw new BasicRuntimeException("Argo Workflows server " + serverUrl + " is not available.");
        }
    }

    public List<String> listWorkflow() {
        List<String> workflows = new ArrayList<>();
        String url = serverUrl + "/api/v1/workflows/" + namespace;
        String body = getClient(url, Method.GET).execute().body();
        workflows.addAll(parseItemNames(body));
        return workflows;
    }

    public List<String> listWorkflowTemplate() {
        List<String> workflowTemplates = new ArrayList<>();
        String body = getClient(serverUrl + "/api/v1/workflow-templates/" + namespace, Method.GET).execute().body();
        workflowTemplates.addAll(parseItemNames(body));
        return workflowTemplates;
    }

    public String submit(String workflow) {
        // TODO 
        return null;
    }

    private List<String> parseItemNames(String body) {
        List<String> itemNames = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(body);
        JSONArray items = jsonObject.getJSONArray("items");
        if (items != null && !items.isEmpty()) {
            for (int i = 0; i < items.size(); i++) {
                JSONObject item = items.getJSONObject(i);
                itemNames.add(item.getJSONObject("metadata").getStr("name"));
            }
        }
        return itemNames;
    }

    private HttpRequest getClient(String url, Method method) {
        HttpRequest request = HttpRequest.of(url).method(method);
        if (StringUtils.hasText(token)) {
            request.bearerAuth(token);
        }
        return request;
    }

    public static void main(String[] args) {
        ArgoSdk argoSdk = new ArgoSdk("https://localhost:2746", "bids-collector", "");
        boolean available = argoSdk.test();
        log.info(available ? "ArgoSdk is available" : "ArgoSdk is not available");
        log.info(argoSdk.listWorkflowTemplate().toString());
        log.info(argoSdk.listWorkflow().toString());
    }
}
