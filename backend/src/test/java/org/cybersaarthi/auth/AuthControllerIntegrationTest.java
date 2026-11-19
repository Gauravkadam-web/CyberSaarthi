package org.cybersaarthi.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cybersaarthi.auth.dto.LoginRequest;
import org.cybersaarthi.auth.dto.RegisterRequest;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setupDatabase() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        userRole = roleRepository.save(new Role("ROLE_USER", "Standard registered user"));
        adminRole = roleRepository.save(new Role("ROLE_ADMIN", "Portal administrator"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register should create new user and return JWT token")
    void shouldRegisterNewUser() throws Exception {
        RegisterRequest request = new RegisterRequest(
            "priya.sharma@example.com",
            "SecurePass@2026",
            "Priya Sharma"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.accessToken", notNullValue()))
            .andExpect(jsonPath("$.data.tokenType", is("Bearer")))
            .andExpect(jsonPath("$.data.user.email", is("priya.sharma@example.com")))
            .andExpect(jsonPath("$.data.user.displayName", is("Priya Sharma")))
            .andExpect(jsonPath("$.data.user.roles", hasItem("ROLE_USER")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register with duplicate email should return 409 Conflict")
    void shouldRejectDuplicateEmailRegistration() throws Exception {
        User existingUser = new User("duplicate@example.com", passwordEncoder.encode("Pass@1234"), "Existing");
        existingUser.addRole(userRole);
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest("duplicate@example.com", "NewPass@1234", "Duplicate Attempt");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success", is(false)))
            .andExpect(jsonPath("$.error", is("CONFLICT")))
            .andExpect(jsonPath("$.message", containsString("already registered")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register with invalid data should return 400 Bad Request with field errors")
    void shouldRejectInvalidRegistrationPayload() throws Exception {
        RegisterRequest request = new RegisterRequest("not-an-email", "short", "");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success", is(false)))
            .andExpect(jsonPath("$.validationErrors", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login with valid credentials should return access token")
    void shouldLoginSuccessfully() throws Exception {
        User user = new User("login.user@example.com", passwordEncoder.encode("ValidPass@123"), "Login User");
        user.addRole(userRole);
        userRepository.save(user);

        LoginRequest request = new LoginRequest("login.user@example.com", "ValidPass@123");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.accessToken", notNullValue()))
            .andExpect(jsonPath("$.data.user.email", is("login.user@example.com")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login with invalid password should return 401 Unauthorized")
    void shouldRejectLoginWithWrongPassword() throws Exception {
        User user = new User("login.fail@example.com", passwordEncoder.encode("CorrectPass@123"), "Fail User");
        user.addRole(userRole);
        userRepository.save(user);

        LoginRequest request = new LoginRequest("login.fail@example.com", "WrongPassword!");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success", is(false)))
            .andExpect(jsonPath("$.message", is("Invalid email or password")));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me without token should return 401 Unauthorized")
    void shouldRejectUnauthenticatedProfileRequest() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me with valid Bearer token should return profile")
    void shouldReturnProfileWithValidToken() throws Exception {
        User user = new User("profile.user@example.com", passwordEncoder.encode("Pass@123"), "Profile User");
        user.addRole(userRole);
        User savedUser = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(new CustomUserDetails(savedUser));

        mockMvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.email", is("profile.user@example.com")))
            .andExpect(jsonPath("$.data.displayName", is("Profile User")))
            .andExpect(jsonPath("$.data.roles", hasItem("ROLE_USER")));
    }

    @Test
    @DisplayName("GET /api/v1/test/admin-only should return 403 Forbidden for ROLE_USER")
    void shouldDenyAdminEndpointForStandardUser() throws Exception {
        User regularUser = new User("regular@example.com", passwordEncoder.encode("Pass@123"), "Regular");
        regularUser.addRole(userRole);
        User savedUser = userRepository.save(regularUser);

        String userToken = jwtTokenProvider.generateToken(new CustomUserDetails(savedUser));

        mockMvc.perform(get("/api/v1/test/admin-only")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success", is(false)))
            .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("GET /api/v1/test/admin-only should return 200 OK for ROLE_ADMIN")
    void shouldAllowAdminEndpointForAdminUser() throws Exception {
        User adminUser = new User("admin@example.com", passwordEncoder.encode("Pass@123"), "Portal Admin");
        adminUser.addRole(adminRole);
        User savedAdmin = userRepository.save(adminUser);

        String adminToken = jwtTokenProvider.generateToken(new CustomUserDetails(savedAdmin));

        mockMvc.perform(get("/api/v1/test/admin-only")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data", is("ADMIN_CONFIDENTIAL_DATA")));
    }
}
