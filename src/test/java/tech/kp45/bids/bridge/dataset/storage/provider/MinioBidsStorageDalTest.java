package tech.kp45.bids.bridge.dataset.storage.provider;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.StringUtils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.collection.dataset.BidsDataset;
import tech.kp45.bids.bridge.common.exception.BasicRuntimeException;
import tech.kp45.bids.bridge.dataset.accessor.BidsCheckMode;
import tech.kp45.bids.bridge.dataset.accessor.BidsStorageAccessor;
import tech.kp45.bids.bridge.dataset.accessor.provider.MinioBidsAccessor;
import tech.kp45.bids.bridge.storage.Storage;

@Slf4j
public class MinioBidsStorageDalTest {
    private String testPath = "ds005616/";

    private MinioBidsAccessor accessor = null;

    @BeforeEach
    public void setUp() {
        Storage storage = new Storage();
        storage.setEndpoint("http://localhost:9000");
        storage.setRegion("us-east-1");
        storage.setBucket("bids");
        String accessKey = System.getenv("LOCAL_MINIO_ACCESS_KEY");
        String secretKey = System.getenv("LOCAL_MINIO_SECRET_KEY");
        if (StringUtils.hasText(secretKey) && StringUtils.hasText(secretKey)) {
            storage.setAccessKey(accessKey);
            storage.setSecretKey(secretKey);
        } else {
            log.error("Access key or secret key is not set");
            throw new BasicRuntimeException("Access key or secret key is not set");
        }
        accessor = new MinioBidsAccessor(storage);
    }

    @Test
    void testDerived() {
        boolean derived = accessor.derived(testPath);
        assertTrue(derived);
    }

    @Test
    void testInitialize() {
        BidsDataset bidsDataset = accessor.initialize(testPath);
        assertTrue("whole-spine".equals(bidsDataset.getName()));
    }

    @Test
    void testListDerivatives() {
        List<String> derivatives = accessor.listDerivatives(testPath);
        assertFalse(derivatives.isEmpty());
    }

    @Test
    void testListDataset() {
        List<String> datasets = accessor.listBidsPath(BidsCheckMode.BIDS_FOLDER_STRUCTURE);
        assertFalse(datasets.isEmpty());
    }

    @Test
    void testListPath() {
        List<String> paths = accessor.listPath(testPath);
        assertFalse(paths.isEmpty());
        assertTrue(paths.contains(testPath + BidsStorageAccessor.BIDS_DESCRIPTION_FILE_NAME));
    }

    @Test
    void testListSub() {
        List<String> subs = accessor.listSub(testPath);
        assertFalse(subs.isEmpty());
    }

    @Test
    void testScanFiles() {
        List<String> files = new ArrayList<>();
        accessor.scanFiles(testPath, files);
        assertFalse(files.isEmpty());
    }

    @Test
    void testGetDescriptorFile() {
        File descriptorFile = accessor.getDescriptorFile(testPath);
        String content = FileUtil.readString(descriptorFile, StandardCharsets.UTF_8);
        assertTrue(JSONUtil.isTypeJSONObject(content));
        JSONObject jsonObject = JSONUtil.parseObj(content);
        assertTrue("whole-spine".equals(jsonObject.getStr("Name")));
    }

    @Test
    void testGetParticipantFile() {
        File participantFile = accessor.getParticipantFile(testPath);
        assertTrue(participantFile.exists());
    }
}
