package tech.kp45.bids.bridge.collection;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DoiUtilsTest {

    private static List<String> samples;

    @BeforeAll
    static void setup() throws Exception {
        Resource resource = new ClassPathResource("openneuro-doi-sample.txt");
        samples = FileUtil.readLines(resource.getFile(), StandardCharsets.UTF_8);
        if (samples.isEmpty()) {
            throw new IllegalStateException("Sample DOI list is empty. Please check the resource file.");
        }
    }

    @Test
    void testGetAccessionNumber() {
        for (String doi : samples) {
            if (DoiUtils.isValidDoi(doi)) {
                String accessionNumber = DoiUtils.getAccessionNumber(doi);
                log.info("Sample DOI: {}, Accession Number: {}", doi, accessionNumber);
                assertTrue(accessionNumber != null && !accessionNumber.isEmpty(),
                        "Expected non-empty accession number for DOI: " + doi);
            } else {
                log.warn("Skipping invalid DOI: {}", doi);
                continue; // Skip invalid DOIs
            }
        }
    }

    @Test
    void testGetOrgInDoi() {
        for (String doi : samples) {
            if (DoiUtils.isValidDoi(doi)) {
                String org = DoiUtils.getOrgInDoi(doi);
                log.info("Sample DOI: {}, Organization: {}", doi, org);
                assertTrue(StringUtils.hasText(org) && org.equals("openneuro"),
                        "Expected non-empty organization for DOI: " + doi);
            } else {
                log.warn("Skipping invalid DOI: {}", doi);
                continue; // Skip invalid DOIs
            }
        }
    }

    @Test
    void testGetVersionFromDoi() {
        for (String doi : samples) {
            if (DoiUtils.isValidDoi(doi)) {
                String version = DoiUtils.getVersionFromDoi(doi);
                log.info("Sample DOI: {}, Version: {}", doi, version);
                assertTrue(StringUtils.hasText(version) && !version.equals("unknown"),
                        "Expected non-empty version for DOI: " + doi);
            } else {
                log.warn("Skipping invalid DOI: {}", doi);
                continue; // Skip invalid DOIs
            }
        }
    }

    @Test
    void testIsValidDoi() {
        String validDoi = "10.18112/openneuro.ds000011.v1.0.0";
        String invalidDoi = "ds000051";
        assertTrue(DoiUtils.isValidDoi(validDoi), "Expected valid DOI to pass validation: " + validDoi);
        assertTrue(!DoiUtils.isValidDoi(invalidDoi), "Expected invalid DOI to fail validation: " + invalidDoi);
    }
}
