package org.cybersaarthi.classification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cybersaarthi.category.entity.IncidentCategory;
import org.cybersaarthi.category.repository.IncidentCategoryRepository;
import org.cybersaarthi.classification.client.MlInferenceClient;
import org.cybersaarthi.classification.dto.MlClassifyResponse;
import org.cybersaarthi.classification.dto.MlInferenceResult;
import org.cybersaarthi.classification.entity.InferenceStatus;
import org.cybersaarthi.classification.repository.MlPredictionRepository;
import org.cybersaarthi.classification.service.ClassificationService;
import org.cybersaarthi.report.dto.CreateReportRequest;
import org.cybersaarthi.report.entity.IncidentChannel;
import org.cybersaarthi.report.repository.IncidentReportRepository;
import org.cybersaarthi.report.service.ReportService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClassificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IncidentReportRepository reportRepository;

    @Autowired
    private MlPredictionRepository mlPredictionRepository;

    @Autowired
    private IncidentCategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ReportService reportService;

    @Autowired
    private ClassificationService classificationService;

    @MockitoBean
    private MlInferenceClient mlInferenceClient;

    private User citizen;
    private User adminUser;
    private String citizenToken;
    private String adminToken;
    private IncidentCategory financialFraudCat;

    @BeforeEach
    void setUp() {
        mlPredictionRepository.deleteAll();
        reportRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        categoryRepository.deleteAll();

        financialFraudCat = new IncidentCategory(
            "1.0",
            "FINANCIAL_FRAUD",
            "Online Financial Fraud",
            "Fraudulent debits, UPI scams, fake investment portals"
        );
        financialFraudCat = categoryRepository.save(financialFraudCat);

        Role userRole = roleRepository.save(new Role("ROLE_USER", "Citizen User"));
        Role adminRole = roleRepository.save(new Role("ROLE_ADMIN", "Admin Reviewer"));

        citizen = new User("citizen.ml@test.org", passwordEncoder.encode("Pass@123"), "Citizen ML");
        citizen.addRole(userRole);
        citizen = userRepository.save(citizen);
        citizenToken = jwtTokenProvider.generateToken(new CustomUserDetails(citizen));

        adminUser = new User("admin.ml@test.org", passwordEncoder.encode("Pass@123"), "Admin ML");
        adminUser.addRole(adminRole);
        adminUser = userRepository.save(adminUser);
        adminToken = jwtTokenProvider.generateToken(new CustomUserDetails(adminUser));
    }

    @Test
    @DisplayName("Report submission with online ML service persists high-confidence advisory prediction")
    void submitReport_onlineMl_persistsSuccessfulPrediction() throws Exception {
        MlClassifyResponse mockResp = new MlClassifyResponse(
            "FINANCIAL_FRAUD",
            0.9850,
            false,
            "predict_proba",
            "baseline-v1.0",
            "1.0",
            true,
            "Advisory classification"
        );
        when(mlInferenceClient.classify(anyString(), anyString()))
            .thenReturn(MlInferenceResult.success(mockResp));

        CreateReportRequest request = new CreateReportRequest(
            IncidentChannel.UPI_APP,
            LocalDate.now(),
            "Received fake message asking to scan QR code which resulted in unauthorised UPI debit.",
            new BigDecimal("3500.00"),
            null,
            true
        );

        mockMvc.perform(post("/api/v1/reports")
                .header("Authorization", "Bearer " + citizenToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.latestPrediction.predictedCategoryCode").value("FINANCIAL_FRAUD"))
            .andExpect(jsonPath("$.data.latestPrediction.inferenceStatus").value("SUCCESS"))
            .andExpect(jsonPath("$.data.latestPrediction.confidenceScore").value(0.9850))
            .andExpect(jsonPath("$.data.latestPrediction.requiresManualReview").value(false))
            .andExpect(jsonPath("$.data.latestPrediction.isAdvisory").value(true));

        assertEquals(1, mlPredictionRepository.count());
        var savedPred = mlPredictionRepository.findAll().get(0);
        assertEquals("FINANCIAL_FRAUD", savedPred.getPredictedCategoryCode());
        assertEquals(InferenceStatus.SUCCESS, savedPred.getInferenceStatus());
        assertFalse(savedPred.isRequiresManualReview());
    }

    @Test
    @DisplayName("NFR-04 Fault Injection: Report submission succeeds even when ML service is offline (FALLBACK_PENDING)")
    void submitReport_mlOffline_submissionSucceedsWithFallback() throws Exception {
        when(mlInferenceClient.classify(anyString(), anyString()))
            .thenReturn(MlInferenceResult.serviceUnavailable("Connection refused to 127.0.0.1:8000"));

        CreateReportRequest request = new CreateReportRequest(
            IncidentChannel.SMS,
            LocalDate.now(),
            "Phishing message received regarding lottery win with an unverified shortened hyperlink.",
            null,
            null,
            true
        );

        // Crucial test: Report submission MUST succeed (201 Created)
        mockMvc.perform(post("/api/v1/reports")
                .header("Authorization", "Bearer " + citizenToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.currentStatus").value("SUBMITTED"))
            .andExpect(jsonPath("$.data.latestPrediction.inferenceStatus").value("SERVICE_UNAVAILABLE"))
            .andExpect(jsonPath("$.data.latestPrediction.requiresManualReview").value(true));

        assertEquals(1, reportRepository.count());
        assertEquals(1, mlPredictionRepository.count());
    }

    @Test
    @DisplayName("Low confidence prediction routes to manual review")
    void submitReport_lowConfidence_routesToManualReview() throws Exception {
        MlClassifyResponse mockResp = new MlClassifyResponse(
            "FINANCIAL_FRAUD",
            0.4200,
            true,
            "predict_proba",
            "baseline-v1.0",
            "1.0",
            true,
            "Advisory classification"
        );
        when(mlInferenceClient.classify(anyString(), anyString()))
            .thenReturn(MlInferenceResult.lowConfidence(mockResp));

        CreateReportRequest request = new CreateReportRequest(
            IncidentChannel.EMAIL,
            LocalDate.now(),
            "Vague email asking to verify my institutional account credentials urgently.",
            null,
            null,
            true
        );

        mockMvc.perform(post("/api/v1/reports")
                .header("Authorization", "Bearer " + citizenToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.latestPrediction.inferenceStatus").value("LOW_CONFIDENCE"))
            .andExpect(jsonPath("$.data.latestPrediction.requiresManualReview").value(true));

        var saved = mlPredictionRepository.findAll().get(0);
        assertTrue(saved.isRequiresManualReview());
    }

    @Test
    @DisplayName("Admin reclassify endpoint executes re-inference and updates prediction record")
    void adminReclassify_asAdmin_updatesPrediction() throws Exception {
        // Initially submitted while ML was offline
        when(mlInferenceClient.classify(anyString(), anyString()))
            .thenReturn(MlInferenceResult.serviceUnavailable("Down"));

        var created = reportService.submitReport(citizen.getId(), new CreateReportRequest(
            IncidentChannel.WEBSITE_PORTAL,
            LocalDate.now(),
            "Suspicious investment portal with guaranteed returns that disappeared overnight.",
            new BigDecimal("10000.00"),
            null,
            true
        ));

        // Now ML comes online
        MlClassifyResponse onlineResp = new MlClassifyResponse(
            "FINANCIAL_FRAUD",
            0.9600,
            false,
            "predict_proba",
            "baseline-v1.0",
            "1.0",
            true,
            "Advisory classification"
        );
        when(mlInferenceClient.classify(anyString(), anyString()))
            .thenReturn(MlInferenceResult.success(onlineResp));

        // Admin triggers reclassification
        mockMvc.perform(post("/api/v1/admin/reports/" + created.id() + "/reclassify")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.predictedCategoryCode").value("FINANCIAL_FRAUD"))
            .andExpect(jsonPath("$.data.inferenceStatus").value("SUCCESS"))
            .andExpect(jsonPath("$.data.confidenceScore").value(0.9600));

        // Citizen trying reclassification gets 403 Forbidden
        mockMvc.perform(post("/api/v1/admin/reports/" + created.id() + "/reclassify")
                .header("Authorization", "Bearer " + citizenToken))
            .andExpect(status().isForbidden());
    }
}
