package tech.kp45.bids.bridge.bff;

import java.util.Map;

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
import tech.kp45.bids.bridge.collection.BidsDataset;
import tech.kp45.bids.bridge.collection.BidsDatasetService;
import tech.kp45.bids.bridge.collection.DoiUtils;
import tech.kp45.bids.bridge.collection.OpenNeuroCollectionTracker;
import tech.kp45.bids.bridge.common.exception.BasicRuntimeException;
import tech.kp45.bids.bridge.dataset.accessor.provider.MinioBidsAccessor;
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
        boolean exist = accessor.exist(accessionNumber + "/");
        if (exist) {
            throw new BasicRuntimeException("Dataset is exist.");
        } else {
            ArgoSdk argoSdk = new ArgoSdk(argoProperties);
            String workflowId = argoSdk.submit("openneuro-collector", Map.of("dataset", accessionNumber));
            log.info("Workflow {} is submitted for dataset {} collection", workflowId, accessionNumber);
            // Store the workflow ID in Redis or a database for tracking
            redisTemplate.opsForValue().set(OpenNeuroCollectionTracker.OPENNEURO_COLLECTION_TRACKER_PREFIX + workflowId,
                    accessionNumber);
        }
    }

    @GetMapping("/api/openneuro/bids")
    public Page<BidsDataset> listOpenNeuroDatasets(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String provider) {

        return bidsDatasetService.listPage(provider, page, size);
    }
}
