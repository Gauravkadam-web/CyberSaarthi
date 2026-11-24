package org.cybersaarthi.analytics.dto;

import java.util.UUID;

public record TopicAggregateDto(
    UUID topicId,
    String topicTitle,
    String categoryName,
    long attemptCount,
    double avgScorePercentage
) {}
