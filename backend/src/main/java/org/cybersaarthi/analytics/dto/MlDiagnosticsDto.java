package org.cybersaarthi.analytics.dto;

import java.math.BigDecimal;
import java.util.Map;

public record MlDiagnosticsDto(
    long totalPredictions,
    long manualReviewRequiredCount,
    BigDecimal avgConfidenceScore,
    Map<String, Long> statusCounts
) {}
