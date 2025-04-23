package tech.kp45.bids.bridge.collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
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
        queryWrapper.eq(BidsDataset::getProvider, provider);
        queryWrapper.eq(BidsDataset::getName, name);
        queryWrapper.eq(BidsDataset::getVersion, version);
        return bidsDatasetMapper.selectCount(queryWrapper) > 0;
    }

    public Page<BidsDataset> listPage(String provider, long page, long size) {
        // 确保分页参数有效
        page = Math.max(1, page);
        size = Math.max(1, size);
        
        // 创建查询条件
        LambdaQueryWrapper<BidsDataset> queryWrapper = new LambdaQueryWrapper<>();
        if (provider != null) {
            queryWrapper.eq(BidsDataset::getProvider, provider);
        }
        
        // 添加排序，确保分页结果一致
        queryWrapper.orderByDesc(BidsDataset::getId);
        
        // 手动获取总记录数
        long total = bidsDatasetMapper.selectCount(queryWrapper);
        
        // 手动计算偏移量
        long offset = (page - 1) * size;
        queryWrapper.last("LIMIT " + offset + ", " + size);
        
        // 创建分页对象并手动设置值
        Page<BidsDataset> result = new Page<>(page, size);
        result.setTotal(total);
        result.setRecords(bidsDatasetMapper.selectList(queryWrapper));
        
        return result;
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
                    boolean exist = exist(bidsDataset.getProvider(), bidsDataset.getName(),
                            bidsDataset.getVersion());
                    if (exist) {
                        continue;
                    }

                    create(bidsDataset);
                } catch (Exception e) {
                    log.error("Failed to parse dataset: {}", datasets.get(i), e);
                }
            }
        }
    }
}
