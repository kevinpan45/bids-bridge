package tech.kp45.bids.bridge.collection;

public enum CollectionStatus {
    /**
     * The collection is still being updated or processed.
     */
    IN_PROGRESS,
    /**
     * The collection is complete.
     */
    COLLECTED,
    /**
     * The collection encountered an error and could not be completed.
     */
    FAILED,
    /**
     * The collection was cancelled before completion.
     */
    CANCELLED;

}
