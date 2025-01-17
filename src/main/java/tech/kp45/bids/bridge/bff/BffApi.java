package tech.kp45.bids.bridge.bff;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.common.exception.BasicRuntimeException;
import tech.kp45.bids.bridge.dataset.storage.BidsCheckMode;
import tech.kp45.bids.bridge.dataset.storage.BidsDataset;
import tech.kp45.bids.bridge.dataset.storage.BidsStorage;
import tech.kp45.bids.bridge.dataset.storage.BidsStorageRegister;
import tech.kp45.bids.bridge.dataset.storage.provider.MinioBidsStorageDal;
import tech.kp45.bids.bridge.dataset.storage.provider.OpenNeuroDal;
import tech.kp45.bids.bridge.job.scheduler.argo.ArgoProperties;
import tech.kp45.bids.bridge.job.scheduler.argo.ArgoSdk;

@Slf4j
@RestController
@CrossOrigin
public class BffApi {

    @Autowired
    private BidsStorageRegister bidsStorageRegister;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private OpenNeuroDal openNeuroDal;

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

    @Autowired
    private ArgoProperties argoProperties;

    @PostMapping("/api/bids/openneuro/{dataset}/collections")
    public void collectOpenNeuroDataset(@PathVariable String dataset) {
        if (!StringUtils.hasText(dataset)) {
            throw new BasicRuntimeException("Dataset cannot be blank");
        }

        boolean exist = openNeuroDal.exist(dataset + "/");
        if (exist) {
            ArgoSdk argoSdk = new ArgoSdk(argoProperties);
            argoSdk.submit("openneuro-collector", Map.of("dataset", dataset));
        } else {
            throw new BasicRuntimeException("Dataset not found");
        }
    }

    @GetMapping("/api/bids/openneuro/datasets")
    public List<BidsDataset> listOpenNeuroDatasets() {
        Set<String> keys = redisTemplate.keys("bids:openneuro:datasets:*");
        List<BidsDataset> datasets = new ArrayList<>();
        if (keys.isEmpty()) {
            datasets = openNeuroDal.load("openneuro/latest.txt");
            log.info("Load {} datasets from local storage", datasets.size());
        } else {
            for (String key : keys) {
                String value = redisTemplate.opsForValue().get(key);
                datasets.add(JSONUtil.toBean(value, BidsDataset.class));
            }
            log.info("Get {} datasets from cache", datasets.size());
        }

        return datasets;
    }
}
