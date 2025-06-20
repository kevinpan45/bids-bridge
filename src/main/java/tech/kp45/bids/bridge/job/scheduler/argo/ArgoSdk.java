package tech.kp45.bids.bridge.job.scheduler.argo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.Method;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.common.exception.BasicRuntimeException;

@Slf4j
public class ArgoSdk {

    private String serverUrl;
    private String namespace;
    private String token;

    public ArgoSdk(ArgoProperties properties) {
        this(properties.getServerUrl(), properties.getNamespace(), properties.getToken());
    }

    public ArgoSdk(String serverUrl, String namespace, String token) {
        if (!StringUtils.hasText(serverUrl) || !StringUtils.hasText(namespace)) {
            throw new BasicRuntimeException("ArgoSdk init failed, apiPath or namespace is empty");
        }
        this.serverUrl = serverUrl;
        this.namespace = namespace;
        if (StringUtils.hasText(token)) {
            this.token = token;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("ArgoSdk is initialized without token.");
            }
        }
    }

    public boolean test() {
        try {
            return HttpRequest.get(serverUrl + "/api/v1/workflows/argo").execute().isOk();
        } catch (Exception e) {
            if (e instanceof IORuntimeException) {
                log.error("Argo Workflows server {} is not reachable of reason [{}]", serverUrl, e.getMessage());
            } else {
                log.error("Argo Workflows server {} request error.", serverUrl, e);
            }
            return false;
        }
    }

    public List<String> listWorkflow() {
        List<String> workflows = new ArrayList<>();
        String url = serverUrl + "/api/v1/workflows/" + namespace;
        String body = getClient(url, Method.GET).execute().body();
        workflows.addAll(parseItemNames(body));
        return workflows;
    }

    public String getWorkflowStatus(String workflow) {
        String url = serverUrl + "/api/v1/workflows/" + namespace + "/" + workflow;
        String body = getClient(url, Method.GET).execute().body();
        JSONObject jsonBody = JSONUtil.parseObj(body);
        if (jsonBody.containsKey("code") && jsonBody.getInt("code") == 5) {
            throw new BasicRuntimeException("Workflow " + workflow + " not found.");
        }
        return jsonBody.getJSONObject("status").getStr("phase");
    }

    public List<String> listWorkflowTemplate() {
        List<String> workflowTemplates = new ArrayList<>();
        String body = getClient(serverUrl + "/api/v1/workflow-templates/" + namespace, Method.GET).execute().body();
        workflowTemplates.addAll(parseItemNames(body));
        return workflowTemplates;
    }

    public String submit(String workflowTemplate, Map<String, Object> parameters) {
        if (!deployed(workflowTemplate)) {
            throw new BasicRuntimeException("Workflow template " + workflowTemplate + " is not deployed.");
        }
        String url = serverUrl + "/api/v1/workflows/" + namespace + "/submit";
        JSONObject body = new JSONObject();
        body.set("namespace", namespace);
        body.set("resourceKind", "WorkflowTemplate");
        body.set("resourceName", workflowTemplate);
        JSONObject submitOptions = generateSubmitBody(parameters);
        body.set("submitOptions", submitOptions);
        String retBody = getClient(url, Method.POST).body(body.toString()).execute().body();
        String workflowId = JSONUtil.parseObj(retBody).getJSONObject("metadata").getStr("name");
        log.info("Workflow {} of template {} is submitted.", workflowId, workflowTemplate);
        return workflowId;
    }

    private boolean deployed(String workflowTemplate) {
        return listWorkflowTemplate().contains(workflowTemplate);
    }

    private JSONObject generateSubmitBody(Map<String, Object> parameters) {
        JSONObject submitOptions = new JSONObject();
        JSONArray formatedParameters = new JSONArray();
        parameters.entrySet().forEach(entry -> {
            formatedParameters.add(entry.getKey() + "=" + entry.getValue());
        });
        submitOptions.set("parameters", formatedParameters);

        return submitOptions;
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

    public boolean workflowTemplateExist(String workflow) {
        List<String> templates = listWorkflowTemplate();
        return templates.contains(workflow);
    }
}
