package org.cybersaarthi.assessment.repository;

import org.cybersaarthi.assessment.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {

    List<QuizAttempt> findAllByUserIdOrderBySubmittedAtDesc(UUID userId);

    Optional<QuizAttempt> findFirstByUserIdAndTopicIdOrderBySubmittedAtDesc(UUID userId, UUID topicId);

    @Query("SELECT COUNT(DISTINCT q.userId) FROM QuizAttempt q")
    long countDistinctParticipants();

    @Query("SELECT COALESCE(AVG((CAST(q.score AS double) / q.maxScore) * 100.0), 0.0) FROM QuizAttempt q WHERE q.maxScore > 0")
    double avgScorePercentage();

    @Query("SELECT COUNT(q) FROM QuizAttempt q WHERE q.maxScore > 0 AND ((CAST(q.score AS double) / q.maxScore) * 100.0) >= 70.0")
    long countPassedAttempts();

    @Query("SELECT q.topicId, COUNT(q), AVG((CAST(q.score AS double) / q.maxScore) * 100.0) FROM QuizAttempt q WHERE q.maxScore > 0 GROUP BY q.topicId")
    List<Object[]> findTopicAggregates();

    @Query("SELECT q FROM QuizAttempt q ORDER BY q.submittedAt DESC")
    List<QuizAttempt> findAllForExport();
}

