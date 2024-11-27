package tech.kp45.bids.bridge.collector.openneuro.sdk;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

public class OpenNeuroSdkTest {
    @Test
    void testGet() {
        OpenNeuroDataset dataset = new OpenNeuroSdk().get("ds005619");
        assertTrue(
                "[18F]SF51, a Novel 18F-labeled PET Radioligand for Translocator Protein 18kDa (TSPO) in Brain, Works Well in Monkeys but Fails in Humans"
                        .equals(dataset.getName()));
    }

    @Test
    void testList() {
        List<OpenNeuroDataset> datasets = new OpenNeuroSdk().list(0, 0);
        assertFalse(datasets.isEmpty());
    }
}
