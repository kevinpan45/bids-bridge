package tech.kp45.bids.bridge.collector;

public abstract class BidsCollector {
    protected abstract boolean available();

    protected abstract void collect();
}
