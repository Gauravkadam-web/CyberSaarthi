package org.cybersaarthi.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cybersaarthi.classification.repository.MlPredictionRepository;
import org.cybersaarthi.report.dto.CreateReportRequest;
import org.cybersaarthi.report.dto.UpdateReportStatusRequest;
import org.cybersaarthi.report.entity.IncidentChannel;
import org.cybersaarthi.report.entity.IncidentReport;
import org.cybersaarthi.report.entity.ReportStatus;
import org.cybersaarthi.report.repository.AuditEventRepository;
import org.cybersaarthi.report.repository.IncidentReportRepository;
import org.cybersaarthi.report.repository.ReportStatusHistoryRepository;
import org.cybersaarthi.report.repository.ReviewNoteRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityBoundaryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IncidentReportRepository reportRepository;

    @Autowired
    private ReportStatusHistoryRepository statusHistoryRepository;

    @Autowired
    private ReviewNoteRepository reviewNoteRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private MlPredictionRepository mlPredictionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User citizenA;
    private User citizenB;
    private User adminUser;
    private String tokenA;
    private String tokenB;
    private String adminToken;

    @BeforeEach
    void setUp() {
        mlPredictionRepository.deleteAll();
        reviewNoteRepository.deleteAll();
        statusHistoryRepository.deleteAll();
        auditEventRepository.deleteAll();
        reportRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role userRole = roleRepository.save(new Role("ROLE_USER", "Standard Citizen"));
        Role adminRole = roleRepository.save(new Role("ROLE_ADMIN", "Administrator"));

        citizenA = new User("alice@test.org", passwordEncoder.encode("SecretPass@123"), "Alice Citizen");
        citizenA.addRole(userRole);
        citizenA = userRepository.save(citizenA);
        tokenA = jwtTokenProvider.generateToken(new CustomUserDetails(citizenA));

        citizenB = new User("bob@test.org", passwordEncoder.encode("SecretPass@123"), "Bob Citizen");
        citizenB.addRole(userRole);
        citizenB = userRepository.save(citizenB);
        tokenB = jwtTokenProvider.generateToken(new CustomUserDetails(citizenB));

        adminUser = new User("security.officer@test.org", passwordEncoder.encode("SecretPass@123"), "Security Officer");
        adminUser.addRole(adminRole);
        adminUser = userRepository.save(adminUser);
        adminToken = jwtTokenProvider.generateToken(new CustomUserDetails(adminUser));
    }

    @Test
    @DisplayName("NFR-01: Unauthenticated requests to protected endpoints return 401 Unauthorized")
    void unauthenticatedRequest_toProtectedEndpoints_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/reports"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/admin/reports"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/admin/analytics/overview"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/admin/analytics/export/incidents"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("NFR-01: Citizen role accessing administrative endpoints returns 403 Forbidden")
    void citizenRole_accessingAdminEndpoints_returns403Forbidden() throws Exception {
        // Review queue listing
        mockMvc.perform(get("/api/v1/admin/reports")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isForbidden());

        // Operational analytics overview
        mockMvc.perform(get("/api/v1/admin/analytics/overview")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isForbidden());

        // Research data exports
        mockMvc.perform(get("/api/v1/admin/analytics/export/incidents")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/admin/analytics/export/assessments")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("NFR-01: IDOR Prevention - Citizen A cannot access Citizen B's incident report")
    void idor_citizenCannotAccessOtherUserReport() throws Exception {
        IncidentReport reportB = new IncidentReport(
                "CS-2026-BBBBB",
                citizenB.getId(),
                IncidentChannel.WHATSAPP,
                LocalDate.of(2026, 3, 10),
                "Bob's confidential report narrative regarding phishing on WhatsApp.",
                new BigDecimal("5000.00"),
                null
        );
        reportB = reportRepository.save(reportB);

        // Citizen A attempts to read Citizen B's report by UUID
        mockMvc.perform(get("/api/v1/reports/" + reportB.getId())
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Access denied: You do not own this incident report")));
    }

    @Test
    @DisplayName("NFR-01: IDOR Prevention - Citizen A cannot transition status on any report")
    void idor_citizenCannotTransitionStatus() throws Exception {
        IncidentReport reportA = new IncidentReport(
                "CS-2026-AAAAA",
                citizenA.getId(),
                IncidentChannel.UPI_APP,
                LocalDate.of(2026, 3, 10),
                "Alice's report narrative on UPI fraud.",
                new BigDecimal("1500.00"),
                null
        );
        reportA = reportRepository.save(reportA);

        UpdateReportStatusRequest request = new UpdateReportStatusRequest(
                ReportStatus.UNDER_REVIEW,
                "Unauthorized transition attempt by citizen",
                null
        );

        mockMvc.perform(patch("/api/v1/admin/reports/" + reportA.getId() + "/status")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("NFR-02: Data Minimisation - Reject narrative containing payment card numbers")
    void dataMinimisation_narrativeContainingCardNumber_rejected() throws Exception {
        CreateReportRequest request = new CreateReportRequest(
                IncidentChannel.UPI_APP,
                LocalDate.now(),
                "I was scammed and entered card 4111 2222 3333 4444 on the payment page.",
                new BigDecimal("1000.00"),
                null,
                true
        );

        mockMvc.perform(post("/api/v1/reports")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Data Minimisation Warning: You appear to have entered a 16-digit payment card number")));
    }

    @Test
    @DisplayName("NFR-02: Data Minimisation - Reject narrative disclosing secret PIN/CVV")
    void dataMinimisation_narrativeDisclosingPin_rejected() throws Exception {
        CreateReportRequest request = new CreateReportRequest(
                IncidentChannel.PHONE_CALL,
                LocalDate.now(),
                "The caller asked for my atm pin 9876 to reverse the charge.",
                new BigDecimal("3000.00"),
                null,
                true
        );

        mockMvc.perform(post("/api/v1/reports")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Data Minimisation Warning: Do not disclose PINs, card CVVs, or banking passwords")));
    }

    @Test
    @DisplayName("NFR-01: SQL Injection Attempt in search parameter is safely parameterised")
    void sqlInjectionAttempt_inSearchParameter_isSafelyParameterised() throws Exception {
        IncidentReport report = new IncidentReport(
                "CS-2026-SAFE1",
                citizenA.getId(),
                IncidentChannel.WEBSITE_PORTAL,
                LocalDate.now(),
                "Genuine suspicious loan app report with documentation.",
                new BigDecimal("2000.00"),
                null
        );
        reportRepository.save(report);

        // SQL injection payload in search string: `' OR '1'='1`
        mockMvc.perform(get("/api/v1/admin/reports")
                .header("Authorization", "Bearer " + adminToken)
                .param("query", "' OR '1'='1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements", is(0))); // treated literally, no matches found
    }

    @Test
    @DisplayName("NFR-01: XSS payload in narrative is stored safely as text and does not disrupt API")
    void xssPayload_inNarrative_storedSafelyAsText() throws Exception {
        String xssPayload = "<script>alert('XSS-TEST')</script>";
        CreateReportRequest request = new CreateReportRequest(
                IncidentChannel.EMAIL,
                LocalDate.now(),
                "Received phishing email with payload " + xssPayload + " from attacker.",
                BigDecimal.ZERO,
                null,
                true
        );

        mockMvc.perform(post("/api/v1/reports")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.referenceCode", notNullValue()));
    }

    @Test
    @DisplayName("NFR-01: Tampered or forged JWT is rejected with 401 Unauthorized")
    void tamperedJwt_rejected() throws Exception {
        String tamperedToken = tokenA + "tamperedSignature123";

        mockMvc.perform(get("/api/v1/reports")
                .header("Authorization", "Bearer " + tamperedToken))
                .andExpect(status().isUnauthorized());
    }
}
