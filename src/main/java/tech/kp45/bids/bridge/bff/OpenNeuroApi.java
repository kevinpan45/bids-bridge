package tech.kp45.bids.bridge.bff;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping("/api/openneuro/{dataset}/collections")
    public void collectOpenNeuroDataset(@PathVariable String dataset, @RequestParam Integer storageId) {
        if (!StringUtils.hasText(dataset)) {
            throw new BasicRuntimeException("Dataset cannot be blank");
        }

        Storage storage = storageService.find(storageId);
        MinioBidsAccessor accessor = new MinioBidsAccessor(storage);
        boolean exist = accessor.exist(dataset + "/");
        if (exist) {
            throw new BasicRuntimeException("Dataset is exist.");
        } else {
            ArgoSdk argoSdk = new ArgoSdk(argoProperties);
            String workflowId = argoSdk.submit("openneuro-collector", Map.of("dataset", dataset));
            log.info("Workflow {} is submitted for dataset {} collection", workflowId, dataset);
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
