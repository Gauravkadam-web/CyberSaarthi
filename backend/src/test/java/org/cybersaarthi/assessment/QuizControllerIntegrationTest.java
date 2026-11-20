package org.cybersaarthi.assessment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cybersaarthi.assessment.dto.SubmitQuizAttemptRequest;
import org.cybersaarthi.assessment.dto.UserAnswerSubmission;
import org.cybersaarthi.assessment.entity.QuizOption;
import org.cybersaarthi.assessment.entity.QuizQuestion;
import org.cybersaarthi.assessment.repository.QuizAttemptRepository;
import org.cybersaarthi.assessment.repository.QuizQuestionRepository;
import org.cybersaarthi.awareness.entity.AwarenessTopic;
import org.cybersaarthi.awareness.entity.PublicationStatus;
import org.cybersaarthi.awareness.repository.AwarenessTopicRepository;
import org.cybersaarthi.security.jwt.JwtTokenProvider;
import org.cybersaarthi.security.user.CustomUserDetails;
import org.cybersaarthi.user.entity.Role;
import org.cybersaarthi.user.entity.User;
import org.cybersaarthi.user.repository.RoleRepository;
import org.cybersaarthi.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QuizControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AwarenessTopicRepository topicRepository;

    @Autowired
    private QuizQuestionRepository questionRepository;

    @Autowired
    private QuizAttemptRepository attemptRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String userToken;
    private AwarenessTopic topic;
    private QuizQuestion q1;
    private QuizQuestion q2;

    @BeforeEach
    void setupData() {
        attemptRepository.deleteAll();
        questionRepository.deleteAll();
        topicRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role userRole = roleRepository.save(new Role("ROLE_USER", "User"));
        User user = new User("quiz.taker@test.org", passwordEncoder.encode("Pass@123"), "Quiz Taker");
        user.addRole(userRole);
        user = userRepository.save(user);
        userToken = jwtTokenProvider.generateToken(new CustomUserDetails(user));

        topic = new AwarenessTopic(
            "quiz-test-topic",
            "Quiz Test Topic",
            "Test Topic Summary",
            "# Content",
            "FINANCIAL_UPI_FRAUD"
        );
        topic.setPublicationStatus(PublicationStatus.PUBLISHED);
        topic.setPublishedAt(Instant.now());
        topic = topicRepository.save(topic);

        // Q1: Correct is B
        q1 = new QuizQuestion(
            topic.getId(),
            "Do you need to enter UPI PIN to receive money?",
            "B",
            "You NEVER enter a PIN to receive money on UPI.",
            1
        );
        q1.addOption(new QuizOption("A", "Yes, to accept payment", 1));
        q1.addOption(new QuizOption("B", "No, never enter PIN to receive", 2));
        q1 = questionRepository.save(q1);

        // Q2: Correct is C
        q2 = new QuizQuestion(
            topic.getId(),
            "What is the official cyber fraud helpline in India?",
            "C",
            "1930 is the national cyber financial fraud helpline.",
            2
        );
        q2.addOption(new QuizOption("A", "100", 1));
        q2.addOption(new QuizOption("B", "112", 2));
        q2.addOption(new QuizOption("C", "1930", 3));
        q2 = questionRepository.save(q2);
    }

    @Test
    @DisplayName("GET /api/v1/quizzes/topics/{id} should return questions WITHOUT leaking correct answers or explanations")
    void shouldReturnClientSafeQuizQuestions() throws Exception {
        mockMvc.perform(get("/api/v1/quizzes/topics/" + topic.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.topicId", is(topic.getId().toString())))
            .andExpect(jsonPath("$.data.questions", hasSize(2)))
            .andExpect(jsonPath("$.data.questions[0].questionText", containsString("UPI PIN")))
            .andExpect(jsonPath("$.data.questions[0].options", hasSize(2)))
            // CRITICAL ANSWER SECRECY INTEGRITY CHECKS
            .andExpect(jsonPath("$.data.questions[0].correctOptionKey").doesNotExist())
            .andExpect(jsonPath("$.data.questions[0].explanation").doesNotExist())
            .andExpect(jsonPath("$.data.questions[1].correctOptionKey").doesNotExist())
            .andExpect(jsonPath("$.data.questions[1].explanation").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/v1/quizzes/attempts should evaluate score deterministically and return feedback")
    void shouldEvaluateQuizAttemptCorrectly() throws Exception {
        // Submit 1 correct (Q1 -> B) and 1 wrong (Q2 -> A, correct is C)
        SubmitQuizAttemptRequest request = new SubmitQuizAttemptRequest(
            topic.getId(),
            List.of(
                new UserAnswerSubmission(q1.getId(), "B"),
                new UserAnswerSubmission(q2.getId(), "A")
            )
        );

        mockMvc.perform(post("/api/v1/quizzes/attempts")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.score", is(1)))
            .andExpect(jsonPath("$.data.maxScore", is(2)))
            .andExpect(jsonPath("$.data.percentage", is(50.0)))
            .andExpect(jsonPath("$.data.feedback", hasSize(2)))
            // Q1 was correct
            .andExpect(jsonPath("$.data.feedback[0].selectedOptionKey", is("B")))
            .andExpect(jsonPath("$.data.feedback[0].correctOptionKey", is("B")))
            .andExpect(jsonPath("$.data.feedback[0].isCorrect", is(true)))
            .andExpect(jsonPath("$.data.feedback[0].explanation", containsString("NEVER enter a PIN")))
            // Q2 was incorrect
            .andExpect(jsonPath("$.data.feedback[1].selectedOptionKey", is("A")))
            .andExpect(jsonPath("$.data.feedback[1].correctOptionKey", is("C")))
            .andExpect(jsonPath("$.data.feedback[1].isCorrect", is(false)))
            .andExpect(jsonPath("$.data.feedback[1].explanation", containsString("1930")));
    }

    @Test
    @DisplayName("POST /api/v1/quizzes/attempts without token should return 401 Unauthorized")
    void shouldRejectUnauthenticatedQuizSubmission() throws Exception {
        SubmitQuizAttemptRequest request = new SubmitQuizAttemptRequest(
            topic.getId(),
            List.of(new UserAnswerSubmission(q1.getId(), "B"))
        );

        mockMvc.perform(post("/api/v1/quizzes/attempts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("GET /api/v1/quizzes/my-attempts should return user past quiz attempts")
    void shouldReturnUserQuizHistory() throws Exception {
        // Submit an attempt first
        SubmitQuizAttemptRequest request = new SubmitQuizAttemptRequest(
            topic.getId(),
            List.of(
                new UserAnswerSubmission(q1.getId(), "B"),
                new UserAnswerSubmission(q2.getId(), "C")
            )
        );

        mockMvc.perform(post("/api/v1/quizzes/attempts")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        // Fetch user attempts
        mockMvc.perform(get("/api/v1/quizzes/my-attempts")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].score", is(2)))
            .andExpect(jsonPath("$.data[0].maxScore", is(2)))
            .andExpect(jsonPath("$.data[0].percentage", is(100.0)))
            .andExpect(jsonPath("$.data[0].topicTitle", is("Quiz Test Topic")));
    }
}
