package tech.kp45.bids.bridge.dataset.service;

import java.util.List;

import tech.kp45.bids.bridge.dataset.Dataset;

public interface DatasetService {
    List<Dataset> listPage(int page, int size);

    Dataset get(Integer id);

    Dataset create(Dataset dataset);

    void delete(Integer id);

    void update(Dataset dataset);
}
