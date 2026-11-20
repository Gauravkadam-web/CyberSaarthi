package org.cybersaarthi.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cybersaarthi.report.dto.AddReviewNoteRequest;
import org.cybersaarthi.report.dto.CreateReportRequest;
import org.cybersaarthi.report.dto.UpdateReportStatusRequest;
import org.cybersaarthi.report.entity.IncidentChannel;
import org.cybersaarthi.report.entity.IncidentReport;
import org.cybersaarthi.report.entity.NoteVisibility;
import org.cybersaarthi.report.entity.ReportStatus;
import org.cybersaarthi.report.repository.AuditEventRepository;
import org.cybersaarthi.report.repository.IncidentReportRepository;
import org.cybersaarthi.report.repository.ReportStatusHistoryRepository;
import org.cybersaarthi.report.repository.ReviewNoteRepository;
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
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportControllerIntegrationTest {

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
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ReportService reportService;

    private User citizenA;
    private User citizenB;
    private User adminUser;
    private String tokenA;
    private String tokenB;
    private String adminToken;

    @BeforeEach
    void setUp() {
        reviewNoteRepository.deleteAll();
        statusHistoryRepository.deleteAll();
        auditEventRepository.deleteAll();
        reportRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role userRole = roleRepository.save(new Role("ROLE_USER", "Standard Citizen"));
        Role adminRole = roleRepository.save(new Role("ROLE_ADMIN", "Administrator"));

        citizenA = new User("citizen.a@test.org", passwordEncoder.encode("Pass@123"), "Citizen A");
        citizenA.addRole(userRole);
        citizenA = userRepository.save(citizenA);
        tokenA = jwtTokenProvider.generateToken(new CustomUserDetails(citizenA));

        citizenB = new User("citizen.b@test.org", passwordEncoder.encode("Pass@123"), "Citizen B");
        citizenB.addRole(userRole);
        citizenB = userRepository.save(citizenB);
        tokenB = jwtTokenProvider.generateToken(new CustomUserDetails(citizenB));

        adminUser = new User("admin.reports@test.org", passwordEncoder.encode("Pass@123"), "Admin Officer");
        adminUser.addRole(adminRole);
        adminUser = userRepository.save(adminUser);
        adminToken = jwtTokenProvider.generateToken(new CustomUserDetails(adminUser));
    }

    @Test
    @DisplayName("POST /api/v1/reports creates report with Crockford reference code and audit trail")
    void submitReport_validPayload_createsReport() throws Exception {
        CreateReportRequest request = new CreateReportRequest(
            IncidentChannel.UPI_APP,
            LocalDate.now().minusDays(1),
            "Received a fraudulent payment request on UPI claiming to be an electricity bill overdue notice.",
            new BigDecimal("2500.00"),
            null,
            true
        );

        mockMvc.perform(post("/api/v1/reports")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value(201))
            .andExpect(jsonPath("$.data.referenceCode", matchesPattern("^CS-\\d{4}-[0-9A-Z]{5}$")))
            .andExpect(jsonPath("$.data.currentStatus").value("SUBMITTED"))
            .andExpect(jsonPath("$.data.incidentChannel").value("UPI_APP"))
            .andExpect(jsonPath("$.data.financialLossAmount").value(2500.00))
            .andExpect(jsonPath("$.data.statusHistory", hasSize(1)))
            .andExpect(jsonPath("$.data.statusHistory[0].toStatus").value("SUBMITTED"));

        assertEquals(1, reportRepository.count());
        assertEquals(1, auditEventRepository.count());
    }

    @Test
    @DisplayName("POST /api/v1/reports rejects 16-digit card number under data minimisation guard")
    void submitReport_withCardNumber_rejected() throws Exception {
        CreateReportRequest request = new CreateReportRequest(
            IncidentChannel.WEBSITE_PORTAL,
            LocalDate.now(),
            "Fraudulent transaction occurred using card 4111 2222 3333 4444 on unknown site.",
            new BigDecimal("1000.00"),
            null,
            true
        );

        mockMvc.perform(post("/api/v1/reports")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("Data Minimisation Warning")));

        assertEquals(0, reportRepository.count());
    }

    @Test
    @DisplayName("POST /api/v1/reports rejects ATM PIN / password keywords under data minimisation guard")
    void submitReport_withSensitiveKeywords_rejected() throws Exception {
        CreateReportRequest request = new CreateReportRequest(
            IncidentChannel.PHONE_CALL,
            LocalDate.now(),
            "Caller asked me to verify my ATM PIN to unlock my frozen bank account.",
            new BigDecimal("0.00"),
            null,
            true
        );

        mockMvc.perform(post("/api/v1/reports")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("Do not disclose PINs")));

        assertEquals(0, reportRepository.count());
    }

    @Test
    @DisplayName("POST /api/v1/reports rejects unacknowledged disclaimer")
    void submitReport_unacknowledgedDisclaimer_rejected() throws Exception {
        CreateReportRequest request = new CreateReportRequest(
            IncidentChannel.EMAIL,
            LocalDate.now(),
            "Received a phishing email asking to reset institutional login credentials.",
            null,
            null,
            false
        );

        mockMvc.perform(post("/api/v1/reports")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/reports returns only authored reports")
    void getMyReports_returnsOnlyOwnReports() throws Exception {
        reportService.submitReport(citizenA.getId(), new CreateReportRequest(
            IncidentChannel.SMS, LocalDate.now(), "Smishing message offering lottery win with link.", null, null, true
        ));
        reportService.submitReport(citizenB.getId(), new CreateReportRequest(
            IncidentChannel.WHATSAPP, LocalDate.now(), "WhatsApp impersonation asking for urgent family medical funds.", null, null, true
        ));

        mockMvc.perform(get("/api/v1/reports")
                .header("Authorization", "Bearer " + tokenA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].incidentChannel").value("SMS"));
    }

    @Test
    @DisplayName("IDOR Guard: Citizen B cannot view Citizen A's report by ID (403 Forbidden)")
    void getReportById_idorAccessAttempt_forbidden() throws Exception {
        var created = reportService.submitReport(citizenA.getId(), new CreateReportRequest(
            IncidentChannel.EMAIL, LocalDate.now(), "Phishing email with attachment claiming to be tax refund.", null, null, true
        ));

        // Citizen A succeeds
        mockMvc.perform(get("/api/v1/reports/" + created.id())
                .header("Authorization", "Bearer " + tokenA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(created.id().toString()));

        // Citizen B gets 403 Forbidden
        mockMvc.perform(get("/api/v1/reports/" + created.id())
                .header("Authorization", "Bearer " + tokenB))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", containsString("Access denied")));
    }

    @Test
    @DisplayName("GET /api/v1/reports/track/{code} allows author to track report")
    void trackReportByReferenceCode_ownerAccess_succeeds() throws Exception {
        var created = reportService.submitReport(citizenA.getId(), new CreateReportRequest(
            IncidentChannel.PHONE_CALL, LocalDate.now(), "Impersonation call claiming credit card reward points expiration.", null, null, true
        ));

        mockMvc.perform(get("/api/v1/reports/track/" + created.referenceCode())
                .header("Authorization", "Bearer " + tokenA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.referenceCode").value(created.referenceCode()));
    }

    @Test
    @DisplayName("Admin Queue: Admin can list reports; Citizen is denied (403)")
    void adminQueue_roleAccessTest() throws Exception {
        reportService.submitReport(citizenA.getId(), new CreateReportRequest(
            IncidentChannel.SOCIAL_MEDIA, LocalDate.now(), "Fake profile impersonating business requesting deposits.", null, null, true
        ));

        // Admin succeeds
        mockMvc.perform(get("/api/v1/admin/reports")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content", hasSize(1)));

        // Citizen fails with 403 Forbidden
        mockMvc.perform(get("/api/v1/admin/reports")
                .header("Authorization", "Bearer " + tokenA))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin status transition: SUBMITTED -> UNDER_REVIEW succeeds; SUBMITTED -> CLOSED fails 400")
    void updateStatus_stateMachineValidation() throws Exception {
        var created = reportService.submitReport(citizenA.getId(), new CreateReportRequest(
            IncidentChannel.UPI_APP, LocalDate.now(), "QR code scam at grocery store leading to unauthorised debit.", new BigDecimal("500.00"), null, true
        ));

        // Invalid jump: SUBMITTED -> CLOSED (Must be reviewed or investigated first)
        UpdateReportStatusRequest invalidRequest = new UpdateReportStatusRequest(
            ReportStatus.CLOSED,
            "Direct closure without review",
            null
        );

        mockMvc.perform(patch("/api/v1/admin/reports/" + created.id() + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("Invalid status transition")));

        // Valid transition: SUBMITTED -> UNDER_REVIEW
        UpdateReportStatusRequest validRequest = new UpdateReportStatusRequest(
            ReportStatus.UNDER_REVIEW,
            "Commencing formal administrative triage and review.",
            null
        );

        mockMvc.perform(patch("/api/v1/admin/reports/" + created.id() + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.currentStatus").value("UNDER_REVIEW"))
            .andExpect(jsonPath("$.data.statusHistory", hasSize(2)));

        assertEquals(2, statusHistoryRepository.count());
    }

    @Test
    @DisplayName("Review Notes: INTERNAL notes visible to admin but completely omitted for citizen")
    void reviewNotes_internalNotePrivacy() throws Exception {
        var created = reportService.submitReport(citizenA.getId(), new CreateReportRequest(
            IncidentChannel.WEBSITE_PORTAL, LocalDate.now(), "Suspicious investment portal offering guaranteed doubling in 3 days.", null, null, true
        ));

        // Admin adds an INTERNAL note
        AddReviewNoteRequest internalNote = new AddReviewNoteRequest(
            "Confidential analysis: domain registered 2 days ago via anonymous registrar.",
            NoteVisibility.INTERNAL
        );

        mockMvc.perform(post("/api/v1/admin/reports/" + created.id() + "/notes")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(internalNote)))
            .andExpect(status().isCreated());

        // Admin adds a USER_VISIBLE note
        AddReviewNoteRequest publicNote = new AddReviewNoteRequest(
            "Report acknowledged. Please also report to 1930 if funds were debited.",
            NoteVisibility.USER_VISIBLE
        );

        mockMvc.perform(post("/api/v1/admin/reports/" + created.id() + "/notes")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(publicNote)))
            .andExpect(status().isCreated());

        // Admin view sees BOTH notes (2 notes)
        mockMvc.perform(get("/api/v1/admin/reports/" + created.id())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.reviewNotes", hasSize(2)));

        // Citizen view sees ONLY the USER_VISIBLE note (1 note)
        mockMvc.perform(get("/api/v1/reports/" + created.id())
                .header("Authorization", "Bearer " + tokenA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.reviewNotes", hasSize(1)))
            .andExpect(jsonPath("$.data.reviewNotes[0].visibility").value("USER_VISIBLE"))
            .andExpect(jsonPath("$.data.reviewNotes[0].noteText", containsString("Please also report to 1930")));
    }
}
