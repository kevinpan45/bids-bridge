package tech.kp45.bids.bridge.storage;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.common.exception.BasicRuntimeException;

@Slf4j
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

    public void create(Storage storage) {
        if (StrUtil.hasBlank(storage.getName(), storage.getProvider(), storage.getEndpoint(), storage.getBucket(),
                storage.getAccessKey(), storage.getSecretKey())) {
            log.error("Storage necessary fields are missing {}", storage);
            throw new BasicRuntimeException("Storage necessary fields are missing");
        }
        storageMapper.insert(storage);
    }

    public void update(Storage storage) {
        if (StrUtil.hasBlank(storage.getName(), storage.getProvider(), storage.getEndpoint(), storage.getBucket(),
                storage.getAccessKey(), storage.getSecretKey())) {
            log.error("Storage necessary fields are missing {}", storage);
            throw new BasicRuntimeException("Storage necessary fields are missing");
        }
        storageMapper.updateById(storage);
    }
}
