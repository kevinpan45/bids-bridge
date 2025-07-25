package tech.kp45.bids.bridge.collection.dataset;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.collection.BidsCollectionConfig;
import tech.kp45.bids.bridge.collection.CollectorConfig;
import tech.kp45.bids.bridge.common.exception.BasicRuntimeException;

@Slf4j
@Service
public class BidsDatasetService {
    @Autowired
    private BidsDatasetMapper bidsDatasetMapper;
    @Autowired
    private BidsCollectionConfig bidsCollectionConfig;

    public void create(BidsDataset dataset) {
        bidsDatasetMapper.insert(dataset);
    }

    public boolean exist(String provider, String name, String version) {
        LambdaQueryWrapper<BidsDataset> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BidsDataset::getProvider, provider).eq(BidsDataset::getName, name).eq(BidsDataset::getVersion,
                version);
        return bidsDatasetMapper.selectCount(queryWrapper) > 0;
    }

    public BidsDataset getInTracking(String provider, String name, String version) {
        LambdaQueryWrapper<BidsDataset> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BidsDataset::getProvider, provider).eq(BidsDataset::getName, name)
                .eq(BidsDataset::getVersion, version);
        return bidsDatasetMapper.selectOne(queryWrapper);
    }

    public Page<BidsDataset> listPage(String provider, long page, long size) {
        LambdaQueryWrapper<BidsDataset> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(BidsDataset::getPublishAt);
        if (provider != null) {
            queryWrapper.eq(BidsDataset::getProvider, provider);
        }
        return bidsDatasetMapper.selectPage(Page.of(page, size), queryWrapper);
    }

    public void startTracking(String provider) {
        CollectorConfig collectorConfig = bidsCollectionConfig.getCollector(provider);
        if (collectorConfig == null) {
            throw new BasicRuntimeException("Collector config not found for provider: " + provider);
        }

        String trackSource = collectorConfig.getSource();
        HttpResponse resp = null;
        try {
            resp = HttpUtil.createGet(trackSource).execute();
        } catch (Exception e) {
            log.error("Failed to fetch datasets from source: {}", trackSource, e);
            return;
        }
        if (resp.isOk()) {
            JSONArray datasets = JSONUtil.parseArray(resp.body());
            log.info("Fetched {} datasets from source: {}", datasets.size(), trackSource);
            for (int i = 0; i < datasets.size(); i++) {
                try {
                    JSONObject gqlObject = datasets.getJSONObject(i);
                    BidsDataset bidsDataset = gqlObject.toBean(BidsDataset.class);
                    String doi = bidsDataset.getDoi();
                    if (StringUtils.hasText(doi) && doi.startsWith("doi:")) {
                        bidsDataset.setDoi(doi.substring(4)); // Remove "doi:" prefix
                    }
                    BidsDataset exist = getInTracking(bidsDataset.getProvider(), bidsDataset.getName(),
                            bidsDataset.getVersion());
                    if (exist != null) {
                        BeanUtil.copyProperties(bidsDataset, exist, "id", "name", "version", "provider");
                        update(exist);
                    } else {
                        create(bidsDataset);
                    }

                } catch (Exception e) {
                    log.error("Failed to parse dataset: {}", datasets.get(i), e);
                }
            }
        } else {
            log.error("Failed to fetch datasets from source: {}. HTTP status: {} and message: {}", trackSource,
                    resp.getStatus(), resp.body());
        }
    }

    public List<BidsDataset> list(String provider) {
        LambdaQueryWrapper<BidsDataset> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(provider)) {
            queryWrapper.eq(BidsDataset::getProvider, provider).orderByDesc(BidsDataset::getPublishAt);
        }
        return bidsDatasetMapper.selectList(queryWrapper);
    }

    public BidsDataset findById(Integer id) {
        return bidsDatasetMapper.selectById(id);
    }

    public void update(BidsDataset bidsDataset) {
        bidsDatasetMapper.updateById(bidsDataset);
    }
}
