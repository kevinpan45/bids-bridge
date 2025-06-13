package tech.kp45.bids.bridge.collection;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DoiUtilsTest {

    String validDoi = "10.18112/openneuro.ds000011.v1.0.0";
    String invalidDoi = "ds000051";

    @Test
    void testGetAccessionNumber() {
        String accessionNumber = DoiUtils.getAccessionNumber(validDoi);
        assertTrue(StringUtils.hasText(accessionNumber) && accessionNumber.equals("ds000011"),
                "Expected accession number 'ds000011' for valid DOI: " + validDoi);
    }

    @Test
    void testGetOrgInDoi() {
        String org = DoiUtils.getOrgInDoi(validDoi);
        assertTrue(StringUtils.hasText(org) && org.equals("openneuro"),
                "Expected organization 'openneuro' for valid DOI: " + validDoi);
    }

    @Test
    void testGetVersionFromDoi() {
        String version = DoiUtils.getVersionFromDoi(validDoi);
        assertTrue(StringUtils.hasText(version) && version.equals("v1.0.0"),
                "Expected version v1.0.0 for valid DOI: " + validDoi);
    }

    @Test
    void testIsValidDoi() {
        assertTrue(DoiUtils.isValidDoi(validDoi), "Expected valid DOI to pass validation: " + validDoi);
        assertTrue(!DoiUtils.isValidDoi(invalidDoi), "Expected invalid DOI to fail validation: " + invalidDoi);
    }
}
