package org.cybersaarthi.assessment.repository;

import org.cybersaarthi.assessment.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {

    List<QuizAttempt> findAllByUserIdOrderBySubmittedAtDesc(UUID userId);

    Optional<QuizAttempt> findFirstByUserIdAndTopicIdOrderBySubmittedAtDesc(UUID userId, UUID topicId);
}
