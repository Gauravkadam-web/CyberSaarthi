package org.cybersaarthi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response payload containing JWT token")
public record AuthResponse(
    String accessToken,
    String tokenType,
    long expiresInMs,
    UserSummaryResponse user
) {
    public static AuthResponse of(String accessToken, long expiresInMs, UserSummaryResponse user) {
        return new AuthResponse(accessToken, "Bearer", expiresInMs, user);
    }
}
