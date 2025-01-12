package com.kraj.tradeapp.core.model;

public enum ProcessingStatus {
    PENDING,
    PROCESSED,
    NOT_APPLICABLE;

    public static ProcessingStatus fromString(String status) {
        for (ProcessingStatus processingStatus : ProcessingStatus.values()) {
            if (processingStatus.name().equalsIgnoreCase(status)) {
                return processingStatus;
            }
        }
        return NOT_APPLICABLE;
    }
}
