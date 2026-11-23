package org.cybersaarthi.classification.entity;

public enum InferenceStatus {
    SUCCESS,
    LOW_CONFIDENCE,
    FALLBACK_PENDING,
    SERVICE_UNAVAILABLE,
    TIMEOUT,
    FAILED
}
