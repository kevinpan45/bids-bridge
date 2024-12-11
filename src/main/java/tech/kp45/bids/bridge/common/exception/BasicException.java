package tech.kp45.bids.bridge.common.exception;

public class BasicException extends Exception {
    public BasicException(String message) {
        super(message);
    }

    public BasicException(String message, Exception e) {
        super(message, e);
    }
}
