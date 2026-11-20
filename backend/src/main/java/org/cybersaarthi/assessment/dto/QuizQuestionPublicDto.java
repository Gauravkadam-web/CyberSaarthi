package org.cybersaarthi.assessment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.cybersaarthi.assessment.entity.QuizQuestion;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Schema(description = "Public question view for unsubmitted quiz (does NOT expose correct answer or explanation)")
public record QuizQuestionPublicDto(
    UUID id,
    String questionText,
    int displayOrder,
    List<QuizOptionPublicDto> options
) {
    public static QuizQuestionPublicDto fromEntity(QuizQuestion question) {
        List<QuizOptionPublicDto> publicOptions = question.getOptions().stream()
            .map(QuizOptionPublicDto::fromEntity)
            .collect(Collectors.toList());

        return new QuizQuestionPublicDto(
            question.getId(),
            question.getQuestionText(),
            question.getDisplayOrder(),
            publicOptions
        );
    }
}
