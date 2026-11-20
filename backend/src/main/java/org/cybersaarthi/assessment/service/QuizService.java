package org.cybersaarthi.assessment.service;

import org.cybersaarthi.assessment.dto.QuizAttemptResultDto;
import org.cybersaarthi.assessment.dto.QuizPublicDto;
import org.cybersaarthi.assessment.dto.SubmitQuizAttemptRequest;
import org.cybersaarthi.assessment.dto.UserAttemptSummaryDto;

import java.util.List;
import java.util.UUID;

public interface QuizService {

    QuizPublicDto getQuizForTopic(UUID topicId);

    QuizAttemptResultDto submitAttempt(UUID userId, SubmitQuizAttemptRequest request);

    List<UserAttemptSummaryDto> getUserAttempts(UUID userId);
}
