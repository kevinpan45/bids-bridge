package tech.kp45.bids.bridge.bff;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.common.exception.BasicRuntimeException;
import tech.kp45.bids.bridge.dataset.storage.BidsStorage;
import tech.kp45.bids.bridge.dataset.storage.BidsStorageRegister;
import tech.kp45.bids.bridge.dataset.storage.provider.MinioBidsStorageDal;

@Slf4j
@RestController
public class BffApi {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String BIDS_STORAGE_KEY = "bids:storage:";

    @Autowired
    private BidsStorageRegister bidsStorageRegister;

    @GetMapping("/api/bids/storages")
    public List<BidsStorage> listBidsStorage() {
        return bidsStorageRegister.getStorages();
    }

    @GetMapping("/api/bids/storages/{id}")
    public BidsStorage getBidsStorage(@PathVariable Integer id) {
        return find(id);
    }

    @PutMapping("/api/bids/storages/{id}/datasets")
    public void cacheStorageBids(@PathVariable Integer id) {
        BidsStorage storage = find(id);
        if (storage == null) {
            log.error("Storage {} not found", id);
            throw new BasicRuntimeException("Storage not found");
        }

        MinioBidsStorageDal dal = new MinioBidsStorageDal(storage);
        List<String> datasets = dal.listBidsPath();
        datasets.forEach(dataset -> {
            String key = BIDS_STORAGE_KEY + id + ":" + dataset.replace("/", "");
            redisTemplate.opsForValue().set(key, dataset);
        });
    }

    @GetMapping("/api/bids/storages/{id}/datasets")
    public String listStorageBids(@PathVariable Integer id) {
        BidsStorage storage = find(id);
        if (storage == null) {
            log.error("Storage {} not found", id);
            throw new BasicRuntimeException("Storage not found");
        }

        final List<String> datasets = new ArrayList<>();
        if (redisTemplate.opsForValue().getOperations().hasKey(BIDS_STORAGE_KEY + id + ":*")) {
            redisTemplate.opsForValue().getOperations().keys(BIDS_STORAGE_KEY + id + ":*").forEach(key -> {
                datasets.add(redisTemplate.opsForValue().get(key));
            });
        } else {
            MinioBidsStorageDal dal = new MinioBidsStorageDal(storage);
            dal.listBidsPath().stream().forEach(dataset -> {
                datasets.add(dataset.replace("/", ""));
            });
        }

        return datasets.toString();
    }

    private BidsStorage find(Integer id) {
        return bidsStorageRegister.getStorages().stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null);
    }
}
