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
        if (parts.length > 1) {
            String[] subParts = parts[1].split("\\.");
            if (subParts.length > 0) {
                return subParts[0]; // Extract the organization, e.g., openneuro
            }
        }
        throw new BasicRuntimeException("Unable to extract organization from DOI: " + doi);
    }

    public static String getAccessionNumber(String doi) {
        if (!isValidDoi(doi)) {
            throw new BasicRuntimeException("Invalid DOI format: " + doi);
        }
        // Extract the accession number from the DOI
        String[] parts = doi.split("/");
        if (parts.length > 1) {
            String[] subParts = parts[1].split("\\.");
            if (subParts.length > 1) {
                return subParts[1]; // Extract accession number, e.g., ds000011
            }
        }
        throw new BasicRuntimeException("Unable to extract accession number from DOI: " + doi);
    }

    public static String getVersionFromDoi(String doi) {
        if (!isValidDoi(doi)) {
            throw new BasicRuntimeException("Invalid DOI format: " + doi);
        }
        // Extract the version from the DOI
        String[] parts = doi.split("\\.");
        if (parts.length >= 4) {
            return parts[parts.length - 3] + "." + parts[parts.length - 2] + "." + parts[parts.length - 1]; // e.g., v1.0.0 from 10.18112/openneuro.ds001145.v1.0.0
        } else {
            return "unknown"; // Fallback if the version is not present
        }
    }
}
