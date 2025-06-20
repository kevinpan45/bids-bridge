package tech.kp45.bids.bridge.bff;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.collection.dataset.BidsDataset;
import tech.kp45.bids.bridge.common.exception.BasicRuntimeException;
import tech.kp45.bids.bridge.dataset.Dataset;
import tech.kp45.bids.bridge.dataset.DatasetService;
import tech.kp45.bids.bridge.dataset.accessor.provider.MinioBidsAccessor;
import tech.kp45.bids.bridge.job.Job;
import tech.kp45.bids.bridge.job.JobService;
import tech.kp45.bids.bridge.job.artifact.Artifact;
import tech.kp45.bids.bridge.job.artifact.ArtifactService;
import tech.kp45.bids.bridge.job.scheduler.argo.ArgoProperties;
import tech.kp45.bids.bridge.job.scheduler.argo.ArgoSdk;
import tech.kp45.bids.bridge.pipeline.Pipeline;
import tech.kp45.bids.bridge.pipeline.PipelineService;
import tech.kp45.bids.bridge.storage.Storage;
import tech.kp45.bids.bridge.storage.StorageService;

@Slf4j
@RestController
@CrossOrigin
public class BffApi {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private StorageService storageService;

    @Autowired
    private DatasetService datasetService;

    @PostMapping("/api/storages")
    public void addStorage(@RequestBody Storage storage) {
        storageService.create(storage);
    }

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

    @PutMapping("/api/storages/{id}")
    public void updateStorage(@PathVariable Integer id, @RequestBody Storage storage) {
        storage.setId(id);
        storageService.update(storage);
    }

    @GetMapping("/api/storages/{id}/status")
    public boolean getStorageStatus(@PathVariable Integer id) {
        Storage storage = storageService.find(id);
        if (storage == null) {
            log.error("Storage {} not found", id);
            throw new BasicRuntimeException("Storage not found");
        }

        MinioBidsAccessor accessor = new MinioBidsAccessor(storage);
        return accessor.available();
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

        MinioBidsAccessor accessor = new MinioBidsAccessor(storage);
        List<BidsDataset> datasets = accessor.scan();
        return datasets.size();
    }

    @PutMapping("/api/storages/{id}/datasets")
    public int loadStorageBids(@PathVariable Integer id) {
        Storage storage = storageService.find(id);
        if (storage == null) {
            log.error("Storage {} not found", id);
            throw new BasicRuntimeException("Storage not found");
        }

        MinioBidsAccessor accessor = new MinioBidsAccessor(storage);
        List<Dataset> datasets = new ArrayList<>();
        List<BidsDataset> bidses = accessor.scan();
        for (BidsDataset bidsDataset : bidses) {
            Dataset dataset = bidsDataset.toDataset();
            dataset.setStorageId(id);
            if (dataset.getDoi() == null) {
                dataset.setDoi("-");
            }
            if (!datasetService.exist(dataset.getName(), dataset.getVersion())) {
                datasetService.create(dataset);
                datasets.add(dataset);
            }
        }
        log.info("Load {} datasets from bucket {} of storage {}", datasets.size(), storage.getBucket(),
                storage.getName());
        return datasets.size();
    }

    @GetMapping("/api/datasets")
    public Page<Dataset> listDatasets(@RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        return datasetService.listPage(page, size);
    }

    @GetMapping("/api/datasets/{id}")
    public Dataset getDataset(@PathVariable Integer id) {
        return datasetService.get(id);
    }

    @GetMapping("/api/datasets/{id}/descriptions")
    public JSONObject getDatasetDescription(@PathVariable Integer id) {
        Dataset dataset = datasetService.get(id);
        if (dataset == null) {
            throw new BasicRuntimeException("Dataset not found");
        }
        Integer storageId = dataset.getStorageId();
        Storage storage = storageService.find(storageId);
        if (storage == null) {
            log.error("Storage {} not found", storageId);
            throw new BasicRuntimeException("Storage not found");
        }
        MinioBidsAccessor accessor = new MinioBidsAccessor(storage);
        File file = accessor.getDescriptorFile(dataset.getStoragePath());
        return JSONUtil.readJSONObject(file, StandardCharsets.UTF_8);
    }

    @GetMapping("/api/datasets/{id}/files")
    public List<String> listDatasetFiles(@PathVariable Integer id) {
        Dataset dataset = datasetService.get(id);
        if (dataset == null) {
            log.error("Dataset {} not found", id);
            throw new BasicRuntimeException("Dataset not found");
        }
        List<String> files = new ArrayList<>();
        String bidsFilesKey = "bids:dataset:" + id + ":files:";
        Set<String> fileKeys = redisTemplate.keys(bidsFilesKey + "*");
        if (fileKeys.isEmpty()) {
            Integer storageId = dataset.getStorageId();
            Storage storage = storageService.find(storageId);
            if (storage == null) {
                log.error("Storage {} not found", storageId);
                throw new BasicRuntimeException("Storage not found");
            }
            MinioBidsAccessor accessor = new MinioBidsAccessor(storage);
            accessor.scanFiles(dataset.getStoragePath(), files);
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
    private PipelineService pipelineService;

    @Autowired
    private BidsAppsAccessor bidsAppsAccessor;

    @PutMapping("/api/pipelines/collections")
    public void importBidsApps() {
        bidsAppsAccessor.importBidsApps();
    }

    @GetMapping("/api/pipelines")
    public List<Pipeline> listPipelines() {
        return pipelineService.list();
    }

    @GetMapping("/api/pipelines/{id}")
    public Pipeline getPipeline(@PathVariable Integer id) {
        return pipelineService.get(id);
    }

    @PostMapping("/api/pipelines")
    public Pipeline createPipeline(@RequestBody Pipeline pipeline) {
        pipelineService.create(pipeline);
        return pipeline;
    }

    @Autowired
    private JobService jobService;

    @PostMapping("/api/jobs")
    public Job createJob(@RequestParam String name, @RequestParam(required = false) String group,
            @RequestParam Integer pipelineId,
            @RequestParam Integer datasetId) {
        Job job = jobService.create(name, group, pipelineId, datasetId);
        log.info("Job {} with pipeline for dataset {} created", job.getId(), job.getPipelineId(), job.getDatasetId());
        return job;
    }

    @GetMapping("/api/jobs")
    public List<Job> listJobs() {
        return jobService.list();
    }

    @GetMapping("/api/bff/jobs")
    public List<JobView> listJobViews() {
        return jobService.listJobViews();
    }

    @GetMapping("/api/jobs/{id}")
    public Job getJob(@PathVariable Integer id) {
        return jobService.get(id);
    }

    @PostMapping("/api/jobs/{id}/scheduling")
    public void scheduleJob(@PathVariable Integer id) {
        jobService.schedule(id);
    }

    @Autowired
    private ArtifactService artifactService;

    @GetMapping("/api/jobs/{id}/artifacts")
    public Artifact getJobArtifact(@PathVariable Integer id) {
        return artifactService.findByJob(id);
    }

    @PutMapping("/api/jobs/{id}/artifacts")
    public Artifact updateJobArtifact(@PathVariable Integer id) {
        Job job = jobService.get(id);
        if (job == null) {
            throw new BasicRuntimeException("Job not found");
        }
        Artifact artifact = artifactService.findByJob(id);
        if (artifact == null) {
            artifact = new Artifact();
            artifact.setJobId(id);
            artifact.setStoragePath(job.getEngineJobId());
            artifactService.create(artifact);
        }
        return artifact;
    }

    @Autowired
    private ArgoProperties argoProperties;

    @GetMapping("/api/settings/engines")
    public ArgoProperties getArgoProperties() {
        ArgoProperties properties = new ArgoProperties();
        properties.setNamespace(argoProperties.getNamespace());
        properties.setServerUrl(argoProperties.getServerUrl());
        return properties;
    }

    @GetMapping("/api/engines/status")
    public String getArgoServerStatus() {
        ArgoSdk argoSdk = new ArgoSdk(argoProperties);
        return argoSdk.test() ? "UP" : "DOWN";
    }
}
