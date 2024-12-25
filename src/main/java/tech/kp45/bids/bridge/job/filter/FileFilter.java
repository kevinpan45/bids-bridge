package tech.kp45.bids.bridge.job.filter;

import java.util.List;

import lombok.Data;

@Data
public class FileFilter {
    private Integer id;
    private Integer jobId;
    private List<String> fileRegexes;
}
