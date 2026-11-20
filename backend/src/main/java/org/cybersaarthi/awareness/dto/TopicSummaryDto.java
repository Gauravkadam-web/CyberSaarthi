package org.cybersaarthi.awareness.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.cybersaarthi.awareness.entity.AwarenessTopic;
import org.cybersaarthi.awareness.entity.PublicationStatus;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Summary card view of an awareness educational topic")
public record TopicSummaryDto(
    UUID id,
    String slug,
    String title,
    String summary,
    String topicCategory,
    PublicationStatus publicationStatus,
    Instant publishedAt
) {
    public static TopicSummaryDto fromEntity(AwarenessTopic topic) {
        return new TopicSummaryDto(
            topic.getId(),
            topic.getSlug(),
            topic.getTitle(),
            topic.getSummary(),
            topic.getTopicCategory(),
            topic.getPublicationStatus(),
            topic.getPublishedAt()
        );
    }
}
