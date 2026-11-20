package org.cybersaarthi.assessment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Single question answer submission")
public record UserAnswerSubmission(

    @NotNull(message = "Question ID is required")
    UUID questionId,

    @NotBlank(message = "Selected option key is required")
    String selectedOptionKey
) {}
