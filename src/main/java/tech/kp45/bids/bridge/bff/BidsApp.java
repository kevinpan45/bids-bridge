package tech.kp45.bids.bridge.bff;

import lombok.Data;

@Data
public class BidsApp {
    private String name;
    private String version;
    private String workflow;
    private String description;
}
