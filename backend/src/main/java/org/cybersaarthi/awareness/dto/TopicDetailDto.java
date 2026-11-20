package org.cybersaarthi.awareness.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.cybersaarthi.awareness.entity.AwarenessTopic;
import org.cybersaarthi.awareness.entity.PublicationStatus;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Full detail view of an awareness educational topic")
public record TopicDetailDto(
    UUID id,
    String slug,
    String title,
    String summary,
    String bodyMarkdown,
    String topicCategory,
    PublicationStatus publicationStatus,
    Instant publishedAt,
    boolean hasQuiz
) {
    public static TopicDetailDto fromEntity(AwarenessTopic topic, boolean hasQuiz) {
        return new TopicDetailDto(
            topic.getId(),
            topic.getSlug(),
            topic.getTitle(),
            topic.getSummary(),
            topic.getBodyMarkdown(),
            topic.getTopicCategory(),
            topic.getPublicationStatus(),
            topic.getPublishedAt(),
            hasQuiz
        );
    }
}
