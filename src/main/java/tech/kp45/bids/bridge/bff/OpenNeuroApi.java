package tech.kp45.bids.bridge.bff;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.collection.Collection;
import tech.kp45.bids.bridge.collection.CollectionService;
import tech.kp45.bids.bridge.collection.CollectionStatus;
import tech.kp45.bids.bridge.collection.DoiUtils;
import tech.kp45.bids.bridge.collection.OpenNeuroCollectionTracker;
import tech.kp45.bids.bridge.collection.dataset.BidsDataset;
import tech.kp45.bids.bridge.collection.dataset.BidsDatasetService;
import tech.kp45.bids.bridge.common.exception.BasicRuntimeException;
import tech.kp45.bids.bridge.dataset.accessor.provider.MinioBidsAccessor;
import tech.kp45.bids.bridge.dataset.accessor.provider.OpenNeuroAccessor;
import tech.kp45.bids.bridge.job.scheduler.argo.ArgoProperties;
import tech.kp45.bids.bridge.job.scheduler.argo.ArgoSdk;
import tech.kp45.bids.bridge.storage.Storage;
import tech.kp45.bids.bridge.storage.StorageService;

@Slf4j
@RestController
@CrossOrigin
public class OpenNeuroApi {
    @Autowired
    private StorageService storageService;
    @Autowired
    private ArgoProperties argoProperties;
    @Autowired
    private BidsDatasetService bidsDatasetService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private CollectionService collectionService;

    @PostMapping("/api/openneuro/{id}/collections")
    public void collectOpenNeuroDataset(@PathVariable Integer id, @RequestParam Integer storageId) {

        BidsDataset bidsDataset = bidsDatasetService.findById(id);
        if (bidsDataset == null) {
            log.error("Dataset with ID {} not found", id);
            throw new BasicRuntimeException("Dataset not found.");
        }

        String doi = bidsDataset.getDoi();
        if (!StringUtils.hasText(doi)) {
            log.error("Dataset DOI is not set for ID {}", id);
            throw new BasicRuntimeException("Dataset DOI is not set.");
        }

        String accessionNumber = doi;

        if (DoiUtils.isValidDoi(doi)) {
            accessionNumber = DoiUtils.getAccessionNumber(doi);
        }

        if (!StringUtils.hasText(accessionNumber)) {
            log.error("Cannot extract OpenNeuro accession number from DOI: {}", doi);
            throw new BasicRuntimeException("Cannot extract OpenNeuro accession number from DOI");
        }

        Storage storage = storageService.find(storageId);
        if (storage == null) {
            log.error("Storage with ID {} not found", storageId);
            throw new BasicRuntimeException("Storage not found.");
        }
        MinioBidsAccessor accessor = new MinioBidsAccessor(storage);
        String storagePath = accessionNumber + "/";
        boolean exist = accessor.exist(storagePath);
        if (exist) {
            throw new BasicRuntimeException("Dataset is exist.");
        } else {
            ArgoSdk argoSdk = new ArgoSdk(argoProperties);
            String workflowId = argoSdk.submit("openneuro-collector", Map.of("dataset", accessionNumber));
            log.info("Workflow {} is submitted for dataset {} collection", workflowId, accessionNumber);

            Collection collection = new Collection();
            String collectionDescription = "OpenNeuro dataset "
                    + accessionNumber + " is collecting to " + storage.getName() + " path " + storagePath;
            collection.setBidsDatasetId(id)
                    .setStorageId(storageId)
                    .setStoragePath(storagePath)
                    .setCollectionExecutionId(workflowId)
                    .setStatus(CollectionStatus.IN_PROGRESS.name())
                    .setDescription(collectionDescription);
            collectionService.create(collection);
        }
    }

    @GetMapping("/api/openneuro/bids")
    public Page<BidsDataset> listOpenNeuroDatasets(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String provider) {

        return bidsDatasetService.listPage(provider, page, size);
    }

    @GetMapping("/api/openneuro/bids/{id}/files")
    public List<String> listOpenNeuroDatasetFiles(@PathVariable Integer id) {
        BidsDataset bidsDataset = bidsDatasetService.findById(id);
        if (bidsDataset == null) {
            log.error("Dataset with ID {} not found", id);
            throw new BasicRuntimeException("Dataset not found.");
        }

        String doi = bidsDataset.getDoi();
        if (!StringUtils.hasText(doi)) {
            log.error("Dataset DOI is not set for ID {}", id);
            throw new BasicRuntimeException("Dataset DOI is not set.");
        }

        String accessionNumber = DoiUtils.getAccessionNumber(doi);
        if (!StringUtils.hasText(accessionNumber)) {
            log.error("Cannot extract OpenNeuro accession number from DOI: {}", doi);
            throw new BasicRuntimeException("Cannot extract OpenNeuro accession number from DOI");
        }

        List<String> files = new ArrayList<>();

        String bidsFilesKey = "openneuro:" + accessionNumber + ":files:";
        Set<String> fileKeys = redisTemplate.keys(bidsFilesKey + "*");
        if (fileKeys.isEmpty()) {
            OpenNeuroAccessor accessor = new OpenNeuroAccessor();
            accessor.scanFiles(accessionNumber, files);
            log.info("Get {} files from OpenNeuro dataset {}", files.size(), accessionNumber);
            files.stream().forEach(file -> {
                String filename = StringUtils.getFilename(file);
                String fileKey = bidsFilesKey + filename;
                redisTemplate.opsForValue().set(fileKey, file);
            });
            log.info("OpenNeuro dataset {} files cached", accessionNumber);
        } else {
            fileKeys.forEach(fileKey -> {
                files.add(redisTemplate.opsForValue().get(fileKey));
            });
        }

        return files;
    }
}
