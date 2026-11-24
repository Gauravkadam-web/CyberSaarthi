package org.cybersaarthi.analytics.dto;

import java.util.List;

public record AssessmentStatsDto(
    long totalAttempts,
    long distinctParticipants,
    long passedAttempts,
    double passRatePercentage,
    double avgScorePercentage,
    List<TopicAggregateDto> topicBreakdowns
) {}
