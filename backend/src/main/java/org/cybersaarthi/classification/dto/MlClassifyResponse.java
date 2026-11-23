package org.cybersaarthi.classification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MlClassifyResponse(
    @JsonProperty("predicted_category") String predictedCategory,
    @JsonProperty("confidence_score") Double confidenceScore,
    @JsonProperty("requires_manual_review") Boolean requiresManualReview,
    @JsonProperty("score_type") String scoreType,
    @JsonProperty("model_version") String modelVersion,
    @JsonProperty("taxonomy_version") String taxonomyVersion,
    @JsonProperty("is_advisory") Boolean isAdvisory,
    String disclaimer
) {}
