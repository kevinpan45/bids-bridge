package tech.kp45.bids.bridge.dataset.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import tech.kp45.bids.bridge.dataset.Dataset;
import tech.kp45.bids.bridge.dataset.dao.DatasetMapper;

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
        LambdaQueryWrapper<Dataset> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dataset::getName, name).eq(Dataset::getVersion, version);
        return datasetMapper.selectCount(queryWrapper) > 0;
    }
}
