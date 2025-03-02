package tech.kp45.bids.bridge.bff;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@CrossOrigin
public class BidsAppsApi {

    @Autowired
    private BidsAppsAccessor bidsAppsAccessor;

    @GetMapping("/api/bids-apps")
    public List<BidsApp> listBidsApps() {
        return bidsAppsAccessor.list();
    }
}
