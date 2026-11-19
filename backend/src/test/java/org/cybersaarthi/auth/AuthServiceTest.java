package org.cybersaarthi.auth;

import org.cybersaarthi.auth.dto.AuthResponse;
import org.cybersaarthi.auth.dto.LoginRequest;
import org.cybersaarthi.auth.dto.RegisterRequest;
import org.cybersaarthi.auth.dto.UserSummaryResponse;
import org.cybersaarthi.auth.service.impl.AuthServiceImpl;
import org.cybersaarthi.common.exception.DuplicateResourceException;
import org.cybersaarthi.common.exception.ResourceNotFoundException;
import org.cybersaarthi.security.jwt.JwtTokenProvider;
import org.cybersaarthi.security.user.CustomUserDetails;
import org.cybersaarthi.user.entity.Role;
import org.cybersaarthi.user.entity.User;
import org.cybersaarthi.user.repository.RoleRepository;
import org.cybersaarthi.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private Role defaultRole;
    private User testUser;

    @BeforeEach
    void setUp() {
        defaultRole = new Role("ROLE_USER", "Standard user");
        testUser = new User("user@example.com", "encodedPassword", "Rohan Sharma");
        testUser.setId(UUID.randomUUID());
        testUser.addRole(defaultRole);
    }

    @Test
    @DisplayName("Should successfully register a new user with default ROLE_USER")
    void shouldRegisterNewUserSuccessfully() {
        RegisterRequest request = new RegisterRequest("new.user@example.com", "Password@123", "New User");

        when(userRepository.existsByEmailIgnoreCase("new.user@example.com")).thenReturn(false);
        when(roleRepository.findByCode("ROLE_USER")).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode(request.password())).thenReturn("encodedHash123");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateToken(any(CustomUserDetails.class))).thenReturn("mock-jwt-token");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("mock-jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.user().displayName()).isEqualTo("Rohan Sharma");

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when registering existing email")
    void shouldThrowWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("existing@example.com", "Password@123", "Existing User");
        when(userRepository.existsByEmailIgnoreCase("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("Email is already registered");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should successfully login user with valid credentials")
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest("user@example.com", "Password@123");
        CustomUserDetails userDetails = new CustomUserDetails(testUser);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userDetails);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtTokenProvider.generateToken(userDetails)).thenReturn("mock-login-token");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(86400000L);
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(testUser));

        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("mock-login-token");
        assertThat(response.user().email()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("Should retrieve current user profile by UUID")
    void shouldGetCurrentUserById() {
        UUID userId = testUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        UserSummaryResponse response = authService.getCurrentUser(userId);

        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.displayName()).isEqualTo("Rohan Sharma");
        assertThat(response.roles()).contains("ROLE_USER");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user UUID does not exist")
    void shouldThrowWhenUserNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getCurrentUser(nonExistentId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User not found");
    }
}
