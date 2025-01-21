package tech.kp45.bids.bridge.storage;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StorageService {
    @Autowired
    private StorageMapper storageMapper;

    public List<Storage> list() {
        return storageMapper.selectList(null);
    }

    public Storage find(Integer id) {
        return storageMapper.selectById(id);
    }
}
