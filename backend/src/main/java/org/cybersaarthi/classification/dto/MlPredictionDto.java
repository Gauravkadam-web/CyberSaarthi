package org.cybersaarthi.classification.dto;

import org.cybersaarthi.classification.entity.InferenceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MlPredictionDto(
    UUID id,
    UUID reportId,
    UUID predictedCategoryId,
    String predictedCategoryCode,
    String predictedCategoryName,
    String modelName,
    String modelVersion,
    String taxonomyVersion,
    BigDecimal confidenceScore,
    String scoreType,
    boolean requiresManualReview,
    InferenceStatus inferenceStatus,
    Instant inferredAt,
    boolean isAdvisory
) {}
