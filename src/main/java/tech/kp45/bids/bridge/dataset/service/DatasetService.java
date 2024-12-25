package tech.kp45.bids.bridge.dataset.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
