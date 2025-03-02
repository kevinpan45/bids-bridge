package tech.kp45.bids.bridge.bff;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import tech.kp45.bids.bridge.pipeline.Pipeline;
import tech.kp45.bids.bridge.pipeline.PipelineService;

@Service
public class BidsAppsAccessor {

    @Autowired
    private PipelineService pipelineService;

    /**
     * Load bids apps from local registry, create pipeline in database if not exists
     */
    public void importBidsApps() {
        JSONArray apps = loadApps();
        apps.forEach(item -> {
            JSONObject app = (JSONObject) item;
            String version = app.getStr("latest_version");
            String workflow = app.getStr("dh") + ":" + version;
            if (pipelineService.findByWorkflow(workflow) == null) {
                Pipeline pipeline = new Pipeline();
                pipeline.setName(app.getStr("gh"));
                pipeline.setVersion(version);
                pipeline.setWorkflow(workflow);
                pipeline.setDescription(app.getStr("description"));
                pipelineService.create(pipeline);
            }
        });
    }

    public List<BidsApp> list() {
        List<BidsApp> bidsApps = new ArrayList<>();
        JSONArray apps = loadApps();
        apps.forEach(item -> {
            JSONObject app = (JSONObject) item;
            String version = app.getStr("latest_version");
            String workflow = app.getStr("dh") + ":" + version;
            BidsApp bidsApp = new BidsApp();
            bidsApp.setName(app.getStr("gh"));
            bidsApp.setVersion(version);
            bidsApp.setWorkflow(workflow);
            bidsApp.setDescription(app.getStr("description"));
            bidsApps.add(bidsApp);
        });
        return bidsApps;
    }

    private JSONArray loadApps() {
        String content = ResourceUtil.readStr("bids-apps/bids-apps.json", StandardCharsets.UTF_8);
        JSONObject json = JSONUtil.parseObj(content);
        return json.getJSONArray("apps");
    }
}
