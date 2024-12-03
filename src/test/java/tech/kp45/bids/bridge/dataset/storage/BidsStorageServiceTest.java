package tech.kp45.bids.bridge.dataset.storage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

public class BidsStorageServiceTest {

    private String testPath = "ds005616/";

    @Test
    void testDerived() {
        boolean derived = new BidsStorageService().derived(testPath);
        assertTrue(derived);
    }

    @Test
    void testInitialize() {
        BidsDataset bidsDataset = new BidsStorageService().initialize(testPath);
        assertTrue("whole-spine".equals(bidsDataset.getName()));
    }

    @Test
    void testListDerivatives() {
        List<String> derivatives = new BidsStorageService().listDerivatives(testPath);
        assertFalse(derivatives.isEmpty());
    }

    @Test
    void testListPath() {
        List<String> paths = new BidsStorageService().listPath(testPath);
        assertFalse(paths.isEmpty());
        assertTrue(paths.contains(testPath + "dataset_description.json"));
    }

    @Test
    void testListSub() {
        List<String> subs = new BidsStorageService().listSub(testPath);
        assertFalse(subs.isEmpty());
    }

    @Test
    void testScanFiles() {
        List<String> files = new ArrayList<>();
        new BidsStorageService().scanFiles(testPath, files);
        assertFalse(files.isEmpty());
    }

    @Test
    void testGetDescriptorFile() {
        File descriptorFile = new BidsStorageService().getDescriptorFile(testPath);
        String content = FileUtil.readString(descriptorFile, StandardCharsets.UTF_8);
        assertTrue(JSONUtil.isTypeJSONObject(content));
        JSONObject jsonObject = JSONUtil.parseObj(content);
        assertTrue("whole-spine".equals(jsonObject.getStr("Name")));
    }

    @Test
    void testGetParticipantFile() {
        File participantFile = new BidsStorageService().getParticipantFile(testPath);
        assertTrue(participantFile.exists());
    }
}
