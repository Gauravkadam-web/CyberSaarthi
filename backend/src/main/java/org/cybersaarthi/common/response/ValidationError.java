package org.cybersaarthi.common.response;

public record ValidationError(
    String field,
    Object rejectedValue,
    String message
) {}
