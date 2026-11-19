package org.cybersaarthi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "User authentication request payload")
public record LoginRequest(

    @Schema(description = "User registered email", example = "user@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a well-formed email address")
    @Size(max = 254, message = "Email cannot exceed 254 characters")
    String email,

    @Schema(description = "User password", example = "P@ssw0rd123!")
    @NotBlank(message = "Password is required")
    String password
) {}
