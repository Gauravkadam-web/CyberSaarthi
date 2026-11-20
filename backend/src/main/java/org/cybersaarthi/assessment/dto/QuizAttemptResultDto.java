package org.cybersaarthi.assessment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Immediate result and educational feedback after quiz submission")
public record QuizAttemptResultDto(
    UUID attemptId,
    UUID topicId,
    int score,
    int maxScore,
    double percentage,
    Instant submittedAt,
    List<QuestionFeedbackDto> feedback
) {}
