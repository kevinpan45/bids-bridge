package tech.kp45.bids.bridge.dataset;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import tech.kp45.bids.bridge.common.exception.BasicRuntimeException;

@Service
public class DatasetService {
    @Autowired
    private DatasetMapper datasetMapper;

    public List<Dataset> list() {
        return datasetMapper.selectList(null);
    }

    public Dataset get(Integer id) {
        return datasetMapper.selectById(id);
    }

    public Dataset create(Dataset dataset) {
        datasetMapper.insert(dataset);
        return dataset;
    }

    public void delete(Integer id) {
        Dataset dataset = get(id);
        dataset.setDeleted(true);
        datasetMapper.updateById(dataset);
    }

    public boolean exist(String name, String version) {
        if (!StringUtils.hasText(name)) {
            throw new BasicRuntimeException("Dataset name is required");
        }
        LambdaQueryWrapper<Dataset> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dataset::getName, name);
        if (StringUtils.hasText(version)) {
            queryWrapper.eq(Dataset::getVersion, version);
        }
        return datasetMapper.selectCount(queryWrapper) > 0;
    }

    public List<Dataset> listByStorage(Integer id) {
        LambdaQueryWrapper<Dataset> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dataset::getStorageId, id);
        return datasetMapper.selectList(queryWrapper);
    }

    public Page<Dataset> listPage(long page, long size) {
        return datasetMapper.selectPage(Page.of(page, size), null);
    }
}
