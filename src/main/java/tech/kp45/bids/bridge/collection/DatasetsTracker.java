package tech.kp45.bids.bridge.collection;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public abstract class DatasetsTracker<T> {
    private static final String BIDS_DATASET_TRACKING_PREFIX = "bids:dataset:openneuro:tracking:";
    public static final String BIDS_DATASET_TACKING_KEY_PATTERN = BIDS_DATASET_TRACKING_PREFIX + "*:metadata";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private BidsDatasetService bidsDatasetService;

    @Scheduled(cron = "0 0 0 * * MON")
    public void trackDatasets() {
        String trackSource = "https://openneuro-dataset-collector.kevinpan45.workers.dev/";
        HttpResponse resp = HttpUtil.createGet(trackSource).execute();
        if (resp.isOk()) {
            JSONArray datasets = JSONUtil.parseArray(resp.body());
            for (int i = 0; i < datasets.size(); i++) {
                try {
                    JSONObject gqlObject = datasets.getJSONObject(i);
                    BidsDataset bidsDataset = gqlObject.toBean(BidsDataset.class);
                    boolean exist = bidsDatasetService.exist(bidsDataset.getProvider(), bidsDataset.getName(),
                            bidsDataset.getVersion());
                    if (exist) {
                        continue;
                    }

                    bidsDatasetService.create(bidsDataset);
                } catch (Exception e) {
                    log.error("Failed to parse dataset: {}", datasets.get(i), e);
                }
            }
        }
    }

    /**
     * 
     * @return all tracking keys for getting dataset metadata
     */
    public Set<String> getTrackingKeys() {
        return redisTemplate.keys(BIDS_DATASET_TACKING_KEY_PATTERN);
    }
}
