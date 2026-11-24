package org.cybersaarthi.analytics.dto;

import java.math.BigDecimal;

public record AnalyticsOverviewDto(
    long totalIncidents,
    long submittedCount,
    long underReviewCount,
    long needsInfoCount,
    long reviewedCount,
    long closedCount,
    BigDecimal totalFinancialLoss,
    BigDecimal avgFinancialLoss,
    long totalQuizAttempts,
    long distinctQuizParticipants,
    double quizPassRatePercentage,
    double avgQuizScorePercentage,
    long totalMlPredictions,
    long mlManualReviewRequiredCount,
    BigDecimal avgMlConfidenceScore
) {}
