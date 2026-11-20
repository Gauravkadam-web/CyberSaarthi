package org.cybersaarthi.assessment.repository;

import org.cybersaarthi.assessment.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, UUID> {

    List<QuizQuestion> findAllByTopicIdAndIsActiveTrueOrderByDisplayOrderAsc(UUID topicId);

    long countByTopicIdAndIsActiveTrue(UUID topicId);
}
