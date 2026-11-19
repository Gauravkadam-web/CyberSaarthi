package org.cybersaarthi.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.cybersaarthi.auth.dto.AuthResponse;
import org.cybersaarthi.auth.dto.LoginRequest;
import org.cybersaarthi.auth.dto.RegisterRequest;
import org.cybersaarthi.auth.dto.UserSummaryResponse;
import org.cybersaarthi.auth.service.AuthService;
import org.cybersaarthi.security.user.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User registration, authentication, and profile endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Registers a new public user with default ROLE_USER and returns a JWT access token.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed for request payload"),
        @ApiResponse(responseCode = "409", description = "Email is already registered")
    })
    public ResponseEntity<org.cybersaarthi.common.response.ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(
            org.cybersaarthi.common.response.ApiResponse.created("User registered successfully", response),
            HttpStatus.CREATED
        );
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Authenticates user credentials and returns a signed JWT access token.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authentication successful"),
        @ApiResponse(responseCode = "400", description = "Invalid request payload format"),
        @ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    public ResponseEntity<org.cybersaarthi.common.response.ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(
            org.cybersaarthi.common.response.ApiResponse.success("Authentication successful", response)
        );
    }

    @GetMapping("/me")
    @Operation(
        summary = "Get current user profile",
        description = "Returns profile details and roles of the currently authenticated user.",
        security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized: Missing or invalid token")
    })
    public ResponseEntity<org.cybersaarthi.common.response.ApiResponse<UserSummaryResponse>> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new org.cybersaarthi.common.exception.UnauthorizedException("Full authentication is required to access this resource");
        }
        UserSummaryResponse response = authService.getCurrentUser(userDetails.getId());
        return ResponseEntity.ok(
            org.cybersaarthi.common.response.ApiResponse.success("User profile retrieved successfully", response)
        );
    }
}
