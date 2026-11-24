package org.cybersaarthi.analytics.dto;

public record CategoryBreakdownDto(
    String categoryCode,
    String categoryName,
    long count,
    String maskedCount,
    double percentage
) {}
