package org.cybersaarthi.assessment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Post-submission question evaluation feedback")
public record QuestionFeedbackDto(
    UUID questionId,
    String questionText,
    String selectedOptionKey,
    String correctOptionKey,
    boolean isCorrect,
    String explanation
) {}
