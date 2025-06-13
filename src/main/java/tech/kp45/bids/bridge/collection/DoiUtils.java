package tech.kp45.bids.bridge.collection;

import org.springframework.util.StringUtils;

import tech.kp45.bids.bridge.common.exception.BasicRuntimeException;

public class DoiUtils {
    /**
     * Validates if the given DOI is in a valid format.
     *
     * @param doi the DOI to validate
     * @return true if the DOI is valid, false otherwise
     */
    public static boolean isValidDoi(String doi) {
        if (!StringUtils.hasText(doi)) {
            return false;
        }
        // A simple regex to check DOI format
        return doi.matches("10\\.\\d{4,9}/[-._;()/:a-zA-Z0-9]+");
    }

    public static String getOrgInDoi(String doi) {
        if (!isValidDoi(doi)) {
            throw new BasicRuntimeException("Invalid DOI format: " + doi);
        }
        // Extract the organization part from the DOI
        String[] parts = doi.split("/");
        if (parts.length < 2) {
            throw new BasicRuntimeException("DOI does not contain organization part: " + doi);
        }
        return parts[1].split("\\.")[0]; // Extract the organization from the DOI
    }

    public static String getAccessionNumber(String doi) {
        if (!isValidDoi(doi)) {
            throw new BasicRuntimeException("Invalid DOI format: " + doi);
        }
        // Extract the accession number from the DOI
        String[] parts = doi.split("\\.");
        if (parts.length > 2) {
            return parts[1] + "." + parts[2]; // e.g., ds001145 from 10.18112/openneuro.ds001145.v1.0.0
        } else {
            return doi; // Fallback if the DOI format is unexpected
        }
    }

    public static String getVersionFromDoi(String doi) {
        if (!isValidDoi(doi)) {
            throw new BasicRuntimeException("Invalid DOI format: " + doi);
        }
        // Extract the version from the DOI
        String[] parts = doi.split("\\.");
        if (parts.length > 3) {
            return parts[3]; // e.g., v1.0.0 from 10.18112/openneuro.ds001145.v1.0.0
        } else {
            return "unknown"; // Fallback if the version is not present
        }
    }
}
