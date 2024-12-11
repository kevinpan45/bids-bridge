package tech.kp45.bids.bridge.bff;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import tech.kp45.bids.bridge.dataset.storage.BidsStorage;
import tech.kp45.bids.bridge.dataset.storage.BidsStorageRegister;

@RestController
public class BffApi {

    @Autowired
    private BidsStorageRegister bidsStorageRegister;

    @GetMapping("/api/bids/storages")
    public List<BidsStorage> listBidsStorage() {
        return bidsStorageRegister.getStorages();
    }

}
