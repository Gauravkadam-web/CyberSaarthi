package org.cybersaarthi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.cybersaarthi.user.entity.AccountStatus;
import org.cybersaarthi.user.entity.Role;
import org.cybersaarthi.user.entity.User;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Schema(description = "User profile summary response")
public record UserSummaryResponse(
    UUID id,
    String email,
    String displayName,
    AccountStatus accountStatus,
    Set<String> roles,
    Instant createdAt
) {
    public static UserSummaryResponse fromEntity(User user) {
        Set<String> roleCodes = user.getRoles().stream()
            .map(Role::getCode)
            .collect(Collectors.toUnmodifiableSet());

        return new UserSummaryResponse(
            user.getId(),
            user.getEmail(),
            user.getDisplayName(),
            user.getAccountStatus(),
            roleCodes,
            user.getCreatedAt()
        );
    }
}
