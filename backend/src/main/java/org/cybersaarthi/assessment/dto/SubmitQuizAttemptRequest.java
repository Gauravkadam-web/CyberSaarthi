package org.cybersaarthi.assessment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

@Schema(description = "Quiz attempt submission request payload")
public record SubmitQuizAttemptRequest(

    @NotNull(message = "Topic ID is required")
    UUID topicId,

    @NotEmpty(message = "Answers list cannot be empty")
    @Valid
    List<UserAnswerSubmission> answers
) {}
