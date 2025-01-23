package tech.kp45.bids.bridge.dataset.storage.provider;

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
import tech.kp45.bids.bridge.dataset.accessor.BidsDataset;
import tech.kp45.bids.bridge.dataset.accessor.BidsStorageAccessor;
import tech.kp45.bids.bridge.dataset.accessor.provider.OpenNeuroAccessor;

public class OpenNeuroDalTest {

    private String testPath = "ds005127/";
    private String expectedName = "AMRI 16-N-0031 sleep1";

    private OpenNeuroAccessor dal = new OpenNeuroAccessor();

    @Test
    void testDerived() {
        boolean derived = dal.derived(testPath);
        assertTrue(derived);
    }

    @Test
    void testInitialize() {
        BidsDataset bidsDataset = dal.initialize(testPath);
        assertTrue(expectedName.equals(bidsDataset.getName()));
    }

    @Test
    void testListDerivatives() {
        List<String> derivatives = dal.listDerivatives(testPath);
        assertFalse(derivatives.isEmpty());
    }

    @Test
    void testListPath() {
        List<String> paths = dal.listPath(testPath);
        assertFalse(paths.isEmpty());
        assertTrue(paths.contains(testPath + BidsStorageAccessor.BIDS_DESCRIPTION_FILE_NAME));
    }

    @Test
    void testListSub() {
        List<String> subs = dal.listSub(testPath);
        assertFalse(subs.isEmpty());
    }

    @Test
    void testScanFiles() {
        List<String> files = new ArrayList<>();
        dal.scanFiles(testPath, files);
        assertFalse(files.isEmpty());
    }

    @Test
    void testGetDescriptorFile() {
        File descriptorFile = dal.getDescriptorFile(testPath);
        String content = FileUtil.readString(descriptorFile, StandardCharsets.UTF_8);
        assertTrue(JSONUtil.isTypeJSONObject(content));
        JSONObject jsonObject = JSONUtil.parseObj(content);
        assertTrue(expectedName.equals(jsonObject.getStr("Name")));
    }

    @Test
    void testGetParticipantFile() {
        File participantFile = dal.getParticipantFile(testPath);
        assertTrue(participantFile.exists());
    }
}
