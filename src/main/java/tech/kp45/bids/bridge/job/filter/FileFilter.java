package tech.kp45.bids.bridge.job.filter;

import java.util.List;

import lombok.Data;

@Data
public class FileFilter {
    private int id;
    private int jobId;
    private List<String> fileRegexes;
}
