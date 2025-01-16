package tech.kp45.bids.bridge.bff;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.common.exception.BasicRuntimeException;
import tech.kp45.bids.bridge.dataset.storage.BidsCheckMode;
import tech.kp45.bids.bridge.dataset.storage.BidsStorage;
import tech.kp45.bids.bridge.dataset.storage.BidsStorageRegister;
import tech.kp45.bids.bridge.dataset.storage.provider.MinioBidsStorageDal;

@Slf4j
@RestController
@CrossOrigin
public class BffApi {

    @Autowired
    private BidsStorageRegister bidsStorageRegister;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("/api/bids/storages")
    public List<BidsStorage> listBidsStorage() {
        List<BidsStorage> storages = bidsStorageRegister.getStorages();
        storages.stream().forEach(s -> {
            if (s != null) {
                s.setAccessKey(null);
                s.setSecretKey(null);
            }
        });
        return storages;
    }

    @GetMapping("/api/bids/storages/{id}")
    public BidsStorage getBidsStorage(@PathVariable Integer id) {
        BidsStorage storage = find(id);
        if (storage != null) {
            storage.setAccessKey(null);
            storage.setSecretKey(null);
        }
        return storage;
    }

    @GetMapping("/api/bids/storages/{id}/datasets")
    public List<String> listStorageBids(@PathVariable Integer id) {
        BidsStorage storage = find(id);
        if (storage == null) {
            log.error("Storage {} not found", id);
            throw new BasicRuntimeException("Storage not found");
        }

        List<String> datasets = new ArrayList<>();

        MinioBidsStorageDal dal = new MinioBidsStorageDal(storage);
        dal.listBidsPath(BidsCheckMode.BIDS_FOLDER_STRUCTURE).stream().forEach(dataset -> {
            datasets.add(dataset.replace("/", ""));
        });

        return datasets;
    }

    @GetMapping("/api/bids/storages/{id}/datasets/{dataset}/files")
    public List<String> listDatasetFiles(@PathVariable Integer id, @PathVariable String dataset) {
        BidsStorage storage = find(id);
        if (storage == null) {
            log.error("Storage {} not found", id);
            throw new BasicRuntimeException("Storage not found");
        }

        List<String> files = new ArrayList<>();
        String bidsFilesKey = "bids:dataset:" + dataset + ":files:";
        Set<String> fileKeys = redisTemplate.keys(bidsFilesKey + "*");
        if (fileKeys.isEmpty()) {
            MinioBidsStorageDal dal = new MinioBidsStorageDal(storage);
            dal.scanFiles(dataset + File.separator, files);
            log.info("Get {} files from dataset {}", files.size(), dataset);
            files.stream().forEach(file -> {
                String filename = StringUtils.getFilename(file);
                String fileKey = bidsFilesKey + filename;
                redisTemplate.opsForValue().set(fileKey, file);
            });
            log.info("Dataset {} files cached", dataset);
        } else {
            fileKeys.forEach(fileKey -> {
                files.add(redisTemplate.opsForValue().get(fileKey));
            });
        }

        return files;
    }

    private BidsStorage find(Integer id) {
        return bidsStorageRegister.getStorages().stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null);
    }
}
