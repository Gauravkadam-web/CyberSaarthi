package org.cybersaarthi.classification.dto;

import org.cybersaarthi.classification.entity.InferenceStatus;

public record MlInferenceResult(
    InferenceStatus status,
    MlClassifyResponse response,
    String errorMessage
) {
    public static MlInferenceResult success(MlClassifyResponse response) {
        return new MlInferenceResult(InferenceStatus.SUCCESS, response, null);
    }

    public static MlInferenceResult lowConfidence(MlClassifyResponse response) {
        return new MlInferenceResult(InferenceStatus.LOW_CONFIDENCE, response, null);
    }

    public static MlInferenceResult fallbackPending(String error) {
        return new MlInferenceResult(InferenceStatus.FALLBACK_PENDING, null, error);
    }

    public static MlInferenceResult serviceUnavailable(String error) {
        return new MlInferenceResult(InferenceStatus.SERVICE_UNAVAILABLE, null, error);
    }

    public static MlInferenceResult timeout(String error) {
        return new MlInferenceResult(InferenceStatus.TIMEOUT, null, error);
    }

    public static MlInferenceResult failed(String error) {
        return new MlInferenceResult(InferenceStatus.FAILED, null, error);
    }
}
