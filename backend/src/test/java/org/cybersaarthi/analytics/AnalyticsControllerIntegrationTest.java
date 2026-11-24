package org.cybersaarthi.analytics;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cybersaarthi.assessment.entity.QuizAttempt;
import org.cybersaarthi.assessment.repository.QuizAttemptRepository;
import org.cybersaarthi.awareness.entity.AwarenessTopic;
import org.cybersaarthi.awareness.entity.PublicationStatus;
import org.cybersaarthi.awareness.repository.AwarenessTopicRepository;
import org.cybersaarthi.category.entity.IncidentCategory;
import org.cybersaarthi.category.repository.IncidentCategoryRepository;
import org.cybersaarthi.classification.entity.InferenceStatus;
import org.cybersaarthi.classification.entity.MlPrediction;
import org.cybersaarthi.classification.repository.MlPredictionRepository;
import org.cybersaarthi.report.entity.AuditEvent;
import org.cybersaarthi.report.entity.IncidentChannel;
import org.cybersaarthi.report.entity.IncidentReport;
import org.cybersaarthi.report.entity.ReportStatus;
import org.cybersaarthi.report.repository.AuditEventRepository;
import org.cybersaarthi.report.repository.IncidentReportRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnalyticsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IncidentReportRepository reportRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private MlPredictionRepository mlPredictionRepository;

    @Autowired
    private IncidentCategoryRepository categoryRepository;

    @Autowired
    private AwarenessTopicRepository topicRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User adminUser;
    private User citizenUser;
    private String adminToken;
    private String citizenToken;

    private IncidentCategory financialFraudCat;
    private AwarenessTopic topic1;

    @BeforeEach
    void setUp() {
        auditEventRepository.deleteAll();
        mlPredictionRepository.deleteAll();
        reportRepository.deleteAll();
        quizAttemptRepository.deleteAll();
        topicRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        categoryRepository.deleteAll();

        Role adminRole = roleRepository.save(new Role("ROLE_ADMIN", "Admin"));
        Role userRole = roleRepository.save(new Role("ROLE_USER", "Citizen"));

        adminUser = new User("admin.analytics@test.org", passwordEncoder.encode("Pass@123"), "Admin Officer");
        adminUser.addRole(adminRole);
        adminUser = userRepository.save(adminUser);
        adminToken = jwtTokenProvider.generateToken(new CustomUserDetails(adminUser));

        citizenUser = new User("citizen.analytics@test.org", passwordEncoder.encode("Pass@123"), "Citizen Person");
        citizenUser.addRole(userRole);
        citizenUser = userRepository.save(citizenUser);
        citizenToken = jwtTokenProvider.generateToken(new CustomUserDetails(citizenUser));

        financialFraudCat = new IncidentCategory("1.0", "FINANCIAL_FRAUD", "Online Financial Fraud", "Scams & debits");
        financialFraudCat = categoryRepository.save(financialFraudCat);

        topic1 = new AwarenessTopic("upi-fraud-prevention", "UPI Fraud Prevention", "Safe UPI practices", "Body text", "FINANCIAL_FRAUD");
        topic1.setCreatedBy(adminUser.getId());
        topic1.setPublicationStatus(PublicationStatus.PUBLISHED);
        topic1 = topicRepository.save(topic1);

        // Seed 2 reports for financial fraud
        IncidentReport r1 = new IncidentReport("CS-2026-A1B2C", citizenUser.getId(), IncidentChannel.UPI_APP, LocalDate.now(), "Description 1", new BigDecimal("1500.00"), financialFraudCat.getId());
        r1.setCurrentStatus(ReportStatus.SUBMITTED);
        reportRepository.save(r1);

        IncidentReport r2 = new IncidentReport("CS-2026-X9Y8Z", citizenUser.getId(), IncidentChannel.PHONE_CALL, LocalDate.now(), "=cmd|' /C calc'!A0", new BigDecimal("2500.00"), financialFraudCat.getId());
        r2.setCurrentStatus(ReportStatus.UNDER_REVIEW);
        reportRepository.save(r2);

        // Seed an ML prediction
        MlPrediction pred = new MlPrediction(
            r1.getId(),
            financialFraudCat.getId(),
            "FINANCIAL_FRAUD",
            "LogisticRegression-TFIDF",
            "baseline-v1.0",
            "1.0",
            new BigDecimal("0.9500"),
            "predict_proba",
            false,
            InferenceStatus.SUCCESS,
            null
        );
        mlPredictionRepository.save(pred);

        // Seed a quiz attempt (score 4 out of 5 = 80%)
        QuizAttempt qa = new QuizAttempt(citizenUser.getId(), topic1.getId(), 4, 5);
        quizAttemptRepository.save(qa);
    }

    @Test
    @DisplayName("GET /api/v1/admin/analytics/overview reconciles exactly with source records")
    void getOverview_reconcilesWithDb() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/overview")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.totalIncidents").value(2))
            .andExpect(jsonPath("$.data.submittedCount").value(1))
            .andExpect(jsonPath("$.data.underReviewCount").value(1))
            .andExpect(jsonPath("$.data.totalFinancialLoss").value(4000.00))
            .andExpect(jsonPath("$.data.avgFinancialLoss").value(2000.00))
            .andExpect(jsonPath("$.data.totalQuizAttempts").value(1))
            .andExpect(jsonPath("$.data.distinctQuizParticipants").value(1))
            .andExpect(jsonPath("$.data.quizPassRatePercentage").value(100.0))
            .andExpect(jsonPath("$.data.totalMlPredictions").value(1))
            .andExpect(jsonPath("$.data.mlManualReviewRequiredCount").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/admin/analytics/overview rejects non-admin users (403 Forbidden)")
    void getOverview_citizenForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/overview")
                .header("Authorization", "Bearer " + citizenToken))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/admin/analytics/categories applies k=5 small-cell suppression (<5)")
    void getCategories_appliesSmallCellSuppression() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/categories")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(greaterThan(0))))
            .andExpect(jsonPath("$.data[?(@.categoryCode == 'FINANCIAL_FRAUD')].count").value(hasItem(2)))
            .andExpect(jsonPath("$.data[?(@.categoryCode == 'FINANCIAL_FRAUD')].maskedCount").value(hasItem("<5")));
    }

    @Test
    @DisplayName("GET /api/v1/admin/analytics/export/incidents outputs de-identified CSV and sanitizes formula injection")
    void exportIncidentsCsv_deIdentifiedAndSanitized() throws Exception {
        var result = mockMvc.perform(get("/api/v1/admin/analytics/export/incidents")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("cybersaarthi_incidents_deidentified.csv")))
            .andExpect(content().contentType("text/csv;charset=UTF-8"))
            .andReturn();

        String csv = result.getResponse().getContentAsString();

        // 1. Must contain header
        assertTrue(csv.contains("Pseudo_Record_ID,Incident_Channel,Incident_YearMonth,Financial_Loss_Bracket"));

        // 2. Must NOT contain direct personal identifiers
        assertFalse(csv.contains("admin.analytics@test.org"));
        assertFalse(csv.contains("citizen.analytics@test.org"));
        assertFalse(csv.contains("Citizen Person"));
        assertFalse(csv.contains(citizenUser.getId().toString()));
        assertFalse(csv.contains("CS-2026-A1B2C")); // Enumerable ref codes omitted

        // 3. Must sanitize formula injection: '=cmd|...' becomes ''=cmd|...'
        assertTrue(csv.contains("R-00001"));
        assertTrue(csv.contains("R-00002"));

        // 4. Must record research export audit event
        assertTrue(auditEventRepository.findAll().stream().anyMatch(e -> e.getEventType().equals("RESEARCH_DATA_EXPORTED")));
    }

    @Test
    @DisplayName("GET /api/v1/admin/analytics/export/incidents rejects non-admin users (403 Forbidden)")
    void exportIncidentsCsv_citizenForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/export/incidents")
                .header("Authorization", "Bearer " + citizenToken))
            .andExpect(status().isForbidden());
    }
}
