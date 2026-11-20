package org.cybersaarthi.awareness.repository;

import org.cybersaarthi.awareness.entity.AwarenessTopic;
import org.cybersaarthi.awareness.entity.PublicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AwarenessTopicRepository extends JpaRepository<AwarenessTopic, UUID> {

    Optional<AwarenessTopic> findBySlug(String slug);

    Optional<AwarenessTopic> findBySlugAndPublicationStatus(String slug, PublicationStatus status);

    boolean existsBySlug(String slug);

    @Query("SELECT t FROM AwarenessTopic t WHERE t.publicationStatus = :status " +
           "AND (:category IS NULL OR t.topicCategory = :category) " +
           "AND (:query IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(t.summary) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY t.publishedAt DESC NULLS LAST, t.createdAt DESC")
    List<AwarenessTopic> findPublishedFiltered(
            @Param("status") PublicationStatus status,
            @Param("category") String category,
            @Param("query") String query
    );
}
