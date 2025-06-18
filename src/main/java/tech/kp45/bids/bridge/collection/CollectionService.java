package tech.kp45.bids.bridge.collection;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CollectionService {
    @Autowired
    private CollectionMapper collectionMapper;

    public void create(Collection collection) {
        collectionMapper.insert(collection);
        log.info("Collection {} is created.", collection.getCollectionExecutionId());
    }

    public List<Collection> list() {
        return collectionMapper.selectList(null);
    }

    public List<Collection> findByStatus(CollectionStatus status) {
        return collectionMapper.selectList(new QueryWrapper<Collection>().eq("status", status.name()));
    }

    public void update(Collection collection) {
        collectionMapper.updateById(collection);
    }
}
