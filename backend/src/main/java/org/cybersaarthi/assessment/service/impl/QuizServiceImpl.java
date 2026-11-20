package org.cybersaarthi.assessment.service.impl;

import org.cybersaarthi.assessment.dto.*;
import org.cybersaarthi.assessment.entity.QuizAttempt;
import org.cybersaarthi.assessment.entity.QuizAttemptAnswer;
import org.cybersaarthi.assessment.entity.QuizQuestion;
import org.cybersaarthi.assessment.repository.QuizAttemptRepository;
import org.cybersaarthi.assessment.repository.QuizQuestionRepository;
import org.cybersaarthi.assessment.service.QuizService;
import org.cybersaarthi.awareness.entity.AwarenessTopic;
import org.cybersaarthi.awareness.repository.AwarenessTopicRepository;
import org.cybersaarthi.common.exception.ApiException;
import org.cybersaarthi.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QuizServiceImpl implements QuizService {

    private static final Logger log = LoggerFactory.getLogger(QuizServiceImpl.class);

    private final AwarenessTopicRepository topicRepository;
    private final QuizQuestionRepository questionRepository;
    private final QuizAttemptRepository attemptRepository;

    public QuizServiceImpl(
            AwarenessTopicRepository topicRepository,
            QuizQuestionRepository questionRepository,
            QuizAttemptRepository attemptRepository) {
        this.topicRepository = topicRepository;
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public QuizPublicDto getQuizForTopic(UUID topicId) {
        AwarenessTopic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new ResourceNotFoundException("AwarenessTopic", "id", topicId));

        List<QuizQuestion> questions = questionRepository.findAllByTopicIdAndIsActiveTrueOrderByDisplayOrderAsc(topicId);
        if (questions.isEmpty()) {
            throw new ResourceNotFoundException("No active quiz questions found for topic: " + topic.getTitle());
        }

        List<QuizQuestionPublicDto> publicQuestions = questions.stream()
            .map(QuizQuestionPublicDto::fromEntity)
            .collect(Collectors.toList());

        return new QuizPublicDto(topic.getId(), topic.getTitle(), publicQuestions);
    }

    @Override
    @Transactional
    public QuizAttemptResultDto submitAttempt(UUID userId, SubmitQuizAttemptRequest request) {
        AwarenessTopic topic = topicRepository.findById(request.topicId())
            .orElseThrow(() -> new ResourceNotFoundException("AwarenessTopic", "id", request.topicId()));

        List<QuizQuestion> questions = questionRepository.findAllByTopicIdAndIsActiveTrueOrderByDisplayOrderAsc(request.topicId());
        if (questions.isEmpty()) {
            throw new ApiException("Cannot submit attempt: topic has no active questions", HttpStatus.BAD_REQUEST);
        }

        Map<UUID, QuizQuestion> questionMap = questions.stream()
            .collect(Collectors.toMap(QuizQuestion::getId, Function.identity()));

        int score = 0;
        int maxScore = questions.size();
        List<QuestionFeedbackDto> feedbackList = new ArrayList<>();
        List<QuizAttemptAnswer> attemptAnswers = new ArrayList<>();

        for (UserAnswerSubmission submission : request.answers()) {
            QuizQuestion question = questionMap.get(submission.questionId());
            if (question == null) {
                continue; // Skip unrecognized questions
            }

            boolean isCorrect = question.getCorrectOptionKey().trim().equalsIgnoreCase(submission.selectedOptionKey().trim());
            if (isCorrect) {
                score++;
            }

            attemptAnswers.add(new QuizAttemptAnswer(question.getId(), submission.selectedOptionKey().trim().toUpperCase(), isCorrect));

            feedbackList.add(new QuestionFeedbackDto(
                question.getId(),
                question.getQuestionText(),
                submission.selectedOptionKey().trim().toUpperCase(),
                question.getCorrectOptionKey(),
                isCorrect,
                question.getExplanation()
            ));
        }

        QuizAttempt attempt = new QuizAttempt(userId, topic.getId(), score, maxScore);
        for (QuizAttemptAnswer ans : attemptAnswers) {
            attempt.addAnswer(ans);
        }

        QuizAttempt savedAttempt = attemptRepository.save(attempt);
        double percentage = maxScore > 0 ? ((double) score / maxScore) * 100.0 : 0.0;

        log.info("Saved quiz attempt ID: {} for user: {} on topic: {} (Score: {}/{})",
            savedAttempt.getId(), userId, topic.getId(), score, maxScore);

        return new QuizAttemptResultDto(
            savedAttempt.getId(),
            topic.getId(),
            score,
            maxScore,
            Math.round(percentage * 100.0) / 100.0,
            savedAttempt.getSubmittedAt(),
            feedbackList
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAttemptSummaryDto> getUserAttempts(UUID userId) {
        List<QuizAttempt> attempts = attemptRepository.findAllByUserIdOrderBySubmittedAtDesc(userId);

        Map<UUID, String> topicTitleCache = new HashMap<>();

        return attempts.stream()
            .map(att -> {
                String title = topicTitleCache.computeIfAbsent(
                    att.getTopicId(),
                    id -> topicRepository.findById(id).map(AwarenessTopic::getTitle).orElse("Unknown Topic")
                );
                double pct = att.getMaxScore() > 0 ? ((double) att.getScore() / att.getMaxScore()) * 100.0 : 0.0;
                return new UserAttemptSummaryDto(
                    att.getId(),
                    att.getTopicId(),
                    title,
                    att.getScore(),
                    att.getMaxScore(),
                    Math.round(pct * 100.0) / 100.0,
                    att.getSubmittedAt()
                );
            })
            .collect(Collectors.toList());
    }
}
