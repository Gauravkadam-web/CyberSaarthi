package org.cybersaarthi.awareness.service;

import org.cybersaarthi.awareness.dto.*;
import org.cybersaarthi.awareness.entity.PublicationStatus;

import java.util.List;
import java.util.UUID;

public interface AwarenessService {

    List<TopicSummaryDto> getPublishedTopics(String category, String query);

    TopicDetailDto getTopicBySlug(String slug, boolean isAdmin);

    TopicDetailDto createTopic(CreateTopicRequest request, UUID adminUserId);

    TopicDetailDto updateTopic(UUID topicId, UpdateTopicRequest request, UUID adminUserId);

    TopicDetailDto updateTopicStatus(UUID topicId, PublicationStatus status, UUID adminUserId);
}
