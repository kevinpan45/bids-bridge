package tech.kp45.bids.bridge.bff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@CrossOrigin
public class BidsAppsApi {

    @Autowired
    private BidsAppsAccessor bidsAppsAccessor;

    @GetMapping("/api/bids-apps")
    public Page<BidsApp> listBidsApps(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        return bidsAppsAccessor.listPage(page, size);
    }
}
