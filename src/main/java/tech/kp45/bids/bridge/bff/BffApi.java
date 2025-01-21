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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.common.exception.BasicRuntimeException;
import tech.kp45.bids.bridge.dataset.Dataset;
import tech.kp45.bids.bridge.dataset.accessor.BidsCheckMode;
import tech.kp45.bids.bridge.dataset.accessor.BidsDataset;
import tech.kp45.bids.bridge.dataset.accessor.provider.MinioBidsStorageDal;
import tech.kp45.bids.bridge.dataset.accessor.provider.OpenNeuroDal;
import tech.kp45.bids.bridge.dataset.service.DatasetService;
import tech.kp45.bids.bridge.job.scheduler.argo.ArgoProperties;
import tech.kp45.bids.bridge.job.scheduler.argo.ArgoSdk;
import tech.kp45.bids.bridge.storage.Storage;
import tech.kp45.bids.bridge.storage.StorageService;

@Slf4j
@RestController
@CrossOrigin
public class BffApi {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private OpenNeuroDal openNeuroDal;

    @Autowired
    private StorageService storageService;

    @Autowired
    private DatasetService datasetService;

    @GetMapping("/api/storages")
    public List<Storage> listBidsStorage() {
        List<Storage> storages = storageService.list();
        storages.stream().forEach(s -> {
            if (s != null) {
                s.setAccessKey(null);
                s.setSecretKey(null);
            }
        });
        return storages;
    }

    @GetMapping("/api/storages/{id}")
    public Storage getBidsStorage(@PathVariable Integer id) {
        Storage storage = storageService.find(id);
        if (storage != null) {
            storage.setAccessKey(null);
            storage.setSecretKey(null);
        }
        return storage;
    }

    @GetMapping("/api/storages/{id}/datasets")
    public List<Dataset> listStorageBids(@PathVariable Integer id) {
        Storage storage = storageService.find(id);
        if (storage == null) {
            log.error("Storage {} not found", id);
            throw new BasicRuntimeException("Storage not found");
        }

        return datasetService.listByStorage(id);
    }

    @PutMapping("/api/storages/{id}/bids")
    public int scanStorageBids(@PathVariable Integer id) {
        Storage storage = storageService.find(id);
        if (storage == null) {
            log.error("Storage {} not found", id);
            throw new BasicRuntimeException("Storage not found");
        }

        MinioBidsStorageDal dal = new MinioBidsStorageDal(storage);
        List<BidsDataset> datasets = dal.scan();
        return datasets.size();
    }

    @PutMapping("/api/storages/{id}/datasets")
    public int loadStorageBids(@PathVariable Integer id) {
        Storage storage = storageService.find(id);
        if (storage == null) {
            log.error("Storage {} not found", id);
            throw new BasicRuntimeException("Storage not found");
        }

        MinioBidsStorageDal dal = new MinioBidsStorageDal(storage);
        List<Dataset> datasets = new ArrayList<>();
        List<BidsDataset> bidses = dal.scan();
        for (BidsDataset bidsDataset : bidses) {
            Dataset dataset = bidsDataset.toDataset();
            if (!datasetService.exist(dataset.getName(), dataset.getVersion())) {
                datasetService.create(dataset);
            }
            datasets.add(dataset);
        }
        return datasets.size();
    }

    @GetMapping("/api/storages/{id}/datasets/{dataset}/files")
    public List<String> listDatasetFiles(@PathVariable Integer id, @PathVariable String dataset) {
        Storage storage = storageService.find(id);
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

    @Autowired
    private ArgoProperties argoProperties;

    @PostMapping("/api/openneuro/{dataset}/collections")
    public void collectOpenNeuroDataset(@PathVariable String dataset) {
        if (!StringUtils.hasText(dataset)) {
            throw new BasicRuntimeException("Dataset cannot be blank");
        }

        boolean exist = openNeuroDal.exist(dataset + "/");
        if (exist) {
            ArgoSdk argoSdk = new ArgoSdk(argoProperties);
            String workflowId = argoSdk.submit("openneuro-collector", Map.of("dataset", dataset));
            log.info("Workflow {} is submitted for dataset {} collection", workflowId, dataset);
        } else {
            throw new BasicRuntimeException("Dataset not found");
        }
    }

    @GetMapping("/api/openneuro/bids")
    public List<BidsDataset> listOpenNeuroDatasets() {
        Set<String> keys = redisTemplate.keys("bids:openneuro:datasets:*");
        List<BidsDataset> datasets = new ArrayList<>();
        if (keys.isEmpty()) {
            datasets = openNeuroDal.scan();
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
