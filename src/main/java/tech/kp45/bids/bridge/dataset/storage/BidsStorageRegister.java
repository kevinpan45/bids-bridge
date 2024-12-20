package tech.kp45.bids.bridge.dataset.storage;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "bids")
public class BidsStorageRegister {
    @Setter
    @Getter
    private List<BidsStorage> storages;
}
