package org.cybersaarthi.awareness.service.impl;

import org.cybersaarthi.assessment.repository.QuizQuestionRepository;
import org.cybersaarthi.awareness.dto.*;
import org.cybersaarthi.awareness.entity.AwarenessTopic;
import org.cybersaarthi.awareness.entity.PublicationStatus;
import org.cybersaarthi.awareness.repository.AwarenessTopicRepository;
import org.cybersaarthi.awareness.service.AwarenessService;
import org.cybersaarthi.common.exception.DuplicateResourceException;
import org.cybersaarthi.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AwarenessServiceImpl implements AwarenessService {

    private static final Logger log = LoggerFactory.getLogger(AwarenessServiceImpl.class);

    private final AwarenessTopicRepository topicRepository;
    private final QuizQuestionRepository quizQuestionRepository;

    public AwarenessServiceImpl(AwarenessTopicRepository topicRepository, QuizQuestionRepository quizQuestionRepository) {
        this.topicRepository = topicRepository;
        this.quizQuestionRepository = quizQuestionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopicSummaryDto> getPublishedTopics(String category, String query) {
        String normalizedCategory = StringUtils.hasText(category) ? category.trim() : null;
        String normalizedQuery = StringUtils.hasText(query) ? query.trim() : null;

        return topicRepository.findPublishedFiltered(PublicationStatus.PUBLISHED, normalizedCategory, normalizedQuery)
            .stream()
            .map(TopicSummaryDto::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TopicDetailDto getTopicBySlug(String slug, boolean isAdmin) {
        AwarenessTopic topic = topicRepository.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Awareness topic not found with slug: " + slug));

        if (!isAdmin && topic.getPublicationStatus() != PublicationStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Awareness topic not found with slug: " + slug);
        }

        boolean hasQuiz = quizQuestionRepository.countByTopicIdAndIsActiveTrue(topic.getId()) > 0;
        return TopicDetailDto.fromEntity(topic, hasQuiz);
    }

    @Override
    @Transactional
    public TopicDetailDto createTopic(CreateTopicRequest request, UUID adminUserId) {
        String slug = StringUtils.hasText(request.slug()) 
            ? slugify(request.slug()) 
            : slugify(request.title());

        if (topicRepository.existsBySlug(slug)) {
            throw new DuplicateResourceException("Topic slug already exists: " + slug);
        }

        AwarenessTopic topic = new AwarenessTopic(
            slug,
            request.title().trim(),
            request.summary().trim(),
            request.bodyMarkdown().trim(),
            request.topicCategory().trim()
        );
        topic.setCreatedBy(adminUserId);
        topic.setUpdatedBy(adminUserId);

        AwarenessTopic savedTopic = topicRepository.save(topic);
        log.info("Created new awareness topic: {} (ID: {})", savedTopic.getSlug(), savedTopic.getId());

        return TopicDetailDto.fromEntity(savedTopic, false);
    }

    @Override
    @Transactional
    public TopicDetailDto updateTopic(UUID topicId, UpdateTopicRequest request, UUID adminUserId) {
        AwarenessTopic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new ResourceNotFoundException("AwarenessTopic", "id", topicId));

        topic.setTitle(request.title().trim());
        topic.setSummary(request.summary().trim());
        topic.setBodyMarkdown(request.bodyMarkdown().trim());
        topic.setTopicCategory(request.topicCategory().trim());
        topic.setUpdatedBy(adminUserId);

        AwarenessTopic updatedTopic = topicRepository.save(topic);
        log.info("Updated awareness topic ID: {}", updatedTopic.getId());

        boolean hasQuiz = quizQuestionRepository.countByTopicIdAndIsActiveTrue(updatedTopic.getId()) > 0;
        return TopicDetailDto.fromEntity(updatedTopic, hasQuiz);
    }

    @Override
    @Transactional
    public TopicDetailDto updateTopicStatus(UUID topicId, PublicationStatus status, UUID adminUserId) {
        AwarenessTopic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new ResourceNotFoundException("AwarenessTopic", "id", topicId));

        topic.setPublicationStatus(status);
        if (status == PublicationStatus.PUBLISHED && topic.getPublishedAt() == null) {
            topic.setPublishedAt(Instant.now());
        }
        topic.setUpdatedBy(adminUserId);

        AwarenessTopic updatedTopic = topicRepository.save(topic);
        log.info("Updated publication status of topic ID {} to {}", updatedTopic.getId(), status);

        boolean hasQuiz = quizQuestionRepository.countByTopicIdAndIsActiveTrue(updatedTopic.getId()) > 0;
        return TopicDetailDto.fromEntity(updatedTopic, hasQuiz);
    }

    private String slugify(String text) {
        return text.toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            .replaceAll("^-|-$", "");
    }
}
