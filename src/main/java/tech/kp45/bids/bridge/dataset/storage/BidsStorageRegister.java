package tech.kp45.bids.bridge.dataset.storage;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import cn.hutool.core.bean.BeanUtil;
import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "bids")
public class BidsStorageRegister {
    @Setter
    private List<BidsStorage> storages;

    public List<BidsStorage> getStorages() {
        List<BidsStorage> desensitization = new ArrayList<>();
        storages.forEach(storage -> {
            BidsStorage clone = new BidsStorage();
            BeanUtil.copyProperties(storage, clone, false);
            clone.setAccessKey(null);
            clone.setSecretKey(null);
            desensitization.add(clone);
        });
        return desensitization;
    }
}
