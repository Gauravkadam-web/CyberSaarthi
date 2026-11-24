package org.cybersaarthi.analytics.dto;

public record ChannelBreakdownDto(
    String channelCode,
    String channelName,
    long count,
    String maskedCount,
    double percentage
) {}
