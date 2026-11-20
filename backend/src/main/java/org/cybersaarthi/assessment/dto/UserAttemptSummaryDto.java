package org.cybersaarthi.assessment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Summary of user past quiz attempt")
public record UserAttemptSummaryDto(
    UUID attemptId,
    UUID topicId,
    String topicTitle,
    int score,
    int maxScore,
    double percentage,
    Instant submittedAt
) {}
