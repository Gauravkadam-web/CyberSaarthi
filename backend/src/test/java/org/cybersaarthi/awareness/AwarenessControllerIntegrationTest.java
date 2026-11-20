package org.cybersaarthi.awareness;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cybersaarthi.awareness.dto.CreateTopicRequest;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AwarenessControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AwarenessTopicRepository topicRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String adminToken;
    private String userToken;
    private AwarenessTopic publishedTopic;
    private AwarenessTopic draftTopic;

    @BeforeEach
    void setupData() {
        topicRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role adminRole = roleRepository.save(new Role("ROLE_ADMIN", "Administrator"));
        Role userRole = roleRepository.save(new Role("ROLE_USER", "User"));

        User admin = new User("admin.awareness@test.org", passwordEncoder.encode("Pass@123"), "Admin");
        admin.addRole(adminRole);
        admin = userRepository.save(admin);
        adminToken = jwtTokenProvider.generateToken(new CustomUserDetails(admin));

        User user = new User("user.awareness@test.org", passwordEncoder.encode("Pass@123"), "User");
        user.addRole(userRole);
        user = userRepository.save(user);
        userToken = jwtTokenProvider.generateToken(new CustomUserDetails(user));

        publishedTopic = new AwarenessTopic(
            "upi-safety-basics",
            "UPI Safety Basics",
            "Essential rules for safe digital payments",
            "# Safe UPI Guide\nNever share PIN.",
            "FINANCIAL_UPI_FRAUD"
        );
        publishedTopic.setPublicationStatus(PublicationStatus.PUBLISHED);
        publishedTopic.setPublishedAt(Instant.now());
        publishedTopic = topicRepository.save(publishedTopic);

        draftTopic = new AwarenessTopic(
            "draft-phishing-guide",
            "Draft Phishing Guide",
            "Internal work in progress",
            "# Phishing Guide (Draft)",
            "PHISHING_SOCIAL_ENGINEERING"
        );
        draftTopic.setPublicationStatus(PublicationStatus.DRAFT);
        draftTopic = topicRepository.save(draftTopic);
    }

    @Test
    @DisplayName("GET /api/v1/awareness/topics should list published topics and exclude drafts")
    void shouldListOnlyPublishedTopics() throws Exception {
        mockMvc.perform(get("/api/v1/awareness/topics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].slug", is("upi-safety-basics")))
            .andExpect(jsonPath("$.data[0].publicationStatus", is("PUBLISHED")));
    }

    @Test
    @DisplayName("GET /api/v1/awareness/topics?category=... should filter by category")
    void shouldFilterTopicsByCategory() throws Exception {
        mockMvc.perform(get("/api/v1/awareness/topics").param("category", "FINANCIAL_UPI_FRAUD"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(1)));

        mockMvc.perform(get("/api/v1/awareness/topics").param("category", "MALWARE_RANSOMWARE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/v1/awareness/topics/{slug} should return published topic details")
    void shouldGetPublishedTopicBySlug() throws Exception {
        mockMvc.perform(get("/api/v1/awareness/topics/upi-safety-basics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.title", is("UPI Safety Basics")))
            .andExpect(jsonPath("$.data.bodyMarkdown", containsString("Never share PIN")));
    }

    @Test
    @DisplayName("GET /api/v1/awareness/topics/{slug} on DRAFT topic by public user should return 404")
    void shouldHideDraftTopicFromPublicUser() throws Exception {
        mockMvc.perform(get("/api/v1/awareness/topics/draft-phishing-guide"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("GET /api/v1/awareness/topics/{slug} on DRAFT topic by ADMIN should return 200")
    void shouldAllowAdminToViewDraftTopic() throws Exception {
        mockMvc.perform(get("/api/v1/awareness/topics/draft-phishing-guide")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.title", is("Draft Phishing Guide")));
    }

    @Test
    @DisplayName("POST /api/v1/admin/awareness/topics as ADMIN should create draft topic")
    void shouldAllowAdminToCreateTopic() throws Exception {
        CreateTopicRequest request = new CreateTopicRequest(
            "New Ransomware Defense",
            "Tips against ransomware lockouts",
            "# Ransomware Defense Guide",
            "MALWARE_RANSOMWARE",
            "new-ransomware-defense"
        );

        mockMvc.perform(post("/api/v1/admin/awareness/topics")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.slug", is("new-ransomware-defense")))
            .andExpect(jsonPath("$.data.publicationStatus", is("DRAFT")));
    }

    @Test
    @DisplayName("POST /api/v1/admin/awareness/topics as standard user should return 403 Forbidden")
    void shouldBlockStandardUserFromCreatingTopic() throws Exception {
        CreateTopicRequest request = new CreateTopicRequest(
            "Unauthorized Topic",
            "Attempt by standard user",
            "# Content",
            "MALWARE_RANSOMWARE",
            null
        );

        mockMvc.perform(post("/api/v1/admin/awareness/topics")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success", is(false)));
    }
}
