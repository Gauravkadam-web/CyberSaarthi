package org.cybersaarthi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "User registration request payload")
public record RegisterRequest(

    @Schema(description = "User email address", example = "user@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a well-formed email address")
    @Size(max = 254, message = "Email cannot exceed 254 characters")
    String email,

    @Schema(description = "Password (minimum 8 characters)", example = "P@ssw0rd123!")
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    String password,

    @Schema(description = "User display name", example = "Rohan Sharma")
    @NotBlank(message = "Display name is required")
    @Size(min = 2, max = 120, message = "Display name must be between 2 and 120 characters")
    String displayName
) {}
