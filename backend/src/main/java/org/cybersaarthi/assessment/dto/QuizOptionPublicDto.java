package org.cybersaarthi.assessment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.cybersaarthi.assessment.entity.QuizOption;

@Schema(description = "Public option view for unsubmitted quiz (does NOT expose correct answer)")
public record QuizOptionPublicDto(
    String optionKey,
    String optionText
) {
    public static QuizOptionPublicDto fromEntity(QuizOption option) {
        return new QuizOptionPublicDto(option.getOptionKey(), option.getOptionText());
    }
}
