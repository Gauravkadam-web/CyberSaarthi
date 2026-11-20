package org.cybersaarthi.assessment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.cybersaarthi.assessment.dto.QuizAttemptResultDto;
import org.cybersaarthi.assessment.dto.QuizPublicDto;
import org.cybersaarthi.assessment.dto.SubmitQuizAttemptRequest;
import org.cybersaarthi.assessment.dto.UserAttemptSummaryDto;
import org.cybersaarthi.assessment.service.QuizService;
import org.cybersaarthi.common.exception.UnauthorizedException;
import org.cybersaarthi.common.response.ApiResponse;
import org.cybersaarthi.security.user.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quizzes")
@Tag(name = "Assessment & Quizzes", description = "Endpoints for educational knowledge quizzes and attempts")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/topics/{topicId}")
    @Operation(
        summary = "Get quiz questions for topic",
        description = "Returns client-safe quiz questions and options without leaking correct answers or explanations."
    )
    public ResponseEntity<ApiResponse<QuizPublicDto>> getQuizForTopic(@PathVariable UUID topicId) {
        QuizPublicDto quiz = quizService.getQuizForTopic(topicId);
        return ResponseEntity.ok(ApiResponse.success("Quiz retrieved successfully", quiz));
    }

    @PostMapping("/attempts")
    @Operation(
        summary = "Submit quiz attempt",
        description = "Submits user answers, calculates score server-side, records attempt, and returns instant score and educational explanations.",
        security = @SecurityRequirement(name = "BearerAuth")
    )
    public ResponseEntity<ApiResponse<QuizAttemptResultDto>> submitAttempt(
            @Valid @RequestBody SubmitQuizAttemptRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new UnauthorizedException("You must be logged in to submit a quiz attempt");
        }

        QuizAttemptResultDto result = quizService.submitAttempt(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Quiz attempt evaluated successfully", result));
    }

    @GetMapping("/my-attempts")
    @Operation(
        summary = "List current user's past quiz attempts",
        description = "Returns a chronological history of quiz scores for the logged-in user.",
        security = @SecurityRequirement(name = "BearerAuth")
    )
    public ResponseEntity<ApiResponse<List<UserAttemptSummaryDto>>> getMyAttempts(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new UnauthorizedException("You must be logged in to view your quiz history");
        }

        List<UserAttemptSummaryDto> attempts = quizService.getUserAttempts(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Quiz history retrieved successfully", attempts));
    }
}
