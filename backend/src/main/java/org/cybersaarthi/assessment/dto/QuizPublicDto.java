package org.cybersaarthi.assessment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Client-safe quiz view for test-taking")
public record QuizPublicDto(
    UUID topicId,
    String topicTitle,
    List<QuizQuestionPublicDto> questions
) {}
