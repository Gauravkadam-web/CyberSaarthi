package org.cybersaarthi.security;

import org.cybersaarthi.security.jwt.JwtTokenProvider;
import org.cybersaarthi.security.user.CustomUserDetails;
import org.cybersaarthi.user.entity.Role;
import org.cybersaarthi.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long EXPIRATION_MS = 3600000; // 1 hour

    private JwtTokenProvider jwtTokenProvider;
    private CustomUserDetails testUserDetails;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(TEST_SECRET, EXPIRATION_MS);

        testUserId = UUID.randomUUID();
        User user = new User("test.user@cybersaarthi.org", "hashedPassword", "Test User");
        user.setId(testUserId);
        user.addRole(new Role("ROLE_USER", "Standard user"));

        testUserDetails = new CustomUserDetails(user);
    }

    @Test
    @DisplayName("Should generate valid signed JWT token containing user details")
    void shouldGenerateValidToken() {
        String token = jwtTokenProvider.generateToken(testUserDetails);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo("test.user@cybersaarthi.org");
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(testUserId);
    }

    @Test
    @DisplayName("Should reject malformed or tampered token")
    void shouldRejectMalformedToken() {
        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.invalid.payload";

        assertThat(jwtTokenProvider.validateToken(invalidToken)).isFalse();
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
        assertThat(jwtTokenProvider.validateToken(null)).isFalse();
    }

    @Test
    @DisplayName("Should reject token signed with different secret key")
    void shouldRejectTokenWithDifferentSecret() {
        String differentSecret = "994E635266556A586E3272357538782F413F4428472B4B6250645367566B5999";
        JwtTokenProvider otherProvider = new JwtTokenProvider(differentSecret, EXPIRATION_MS);

        String token = otherProvider.generateToken(testUserDetails);

        assertThat(jwtTokenProvider.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("Should reject expired token")
    void shouldRejectExpiredToken() {
        JwtTokenProvider expiredProvider = new JwtTokenProvider(TEST_SECRET, -1000); // Already expired

        String token = expiredProvider.generateToken(testUserDetails);

        assertThat(jwtTokenProvider.validateToken(token)).isFalse();
    }
}
