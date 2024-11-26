package tech.kp45.bids.bridge.collector.openneuro;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.collector.BidsCollector;

@Slf4j
@Service
public class OpenNeuroBidsCollector extends BidsCollector {

    @Override
    protected boolean available() {
        throw new UnsupportedOperationException("Unimplemented method 'available'");
    }

    @Override
    protected void collect() {
        throw new UnsupportedOperationException("Unimplemented method 'collect'");
    }

}
