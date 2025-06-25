package tech.kp45.bids.bridge.bff;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BidsApp {
    private String name;
    private String version;
    private String workflow;
    private String description;
}
