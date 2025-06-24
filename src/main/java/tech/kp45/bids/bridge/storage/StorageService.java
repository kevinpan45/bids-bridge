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

    /**
     * Update storage excluding credential fields.
     * 
     * @param storage
     */
    public void update(Storage storage) {
        if (StrUtil.hasBlank(storage.getName(), storage.getProvider(), storage.getEndpoint(), storage.getBucket())) {
            log.error("Storage necessary fields are missing {}", storage);
            throw new BasicRuntimeException("Storage necessary fields are missing");
        }
        Storage exist = storageMapper.selectById(storage.getId());
        if (exist == null) {
            log.error("Storage not found with id {}", storage.getId());
            throw new BasicRuntimeException("Storage not found");
        }
        exist.setName(storage.getName()).setProvider(storage.getProvider()).setEndpoint(storage.getEndpoint())
                .setBucket(storage.getBucket());
        storageMapper.updateById(exist);
    }

    public void updateCredential(Integer id, String accessKey, String secretKey) {
        Storage storage = storageMapper.selectById(id);
        if (storage == null) {
            log.error("Storage not found with id {}", id);
            throw new BasicRuntimeException("Storage not found");
        }
        if (StrUtil.hasBlank(accessKey, secretKey)) {
            log.error("Storage credential fields are missing for id {}", id);
            throw new BasicRuntimeException("Storage credential fields are missing");
        }
        storage.setAccessKey(accessKey);
        storage.setSecretKey(secretKey);
        storageMapper.updateById(storage);
    }
}
