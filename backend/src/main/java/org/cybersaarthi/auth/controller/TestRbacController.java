package org.cybersaarthi.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.cybersaarthi.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
@Tag(name = "Testing / Verification", description = "Endpoints for verifying role-based authorization rules")
public class TestRbacController {

    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin-only test endpoint", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<String>> adminOnly() {
        return ResponseEntity.ok(ApiResponse.success("Access granted to administrator", "ADMIN_CONFIDENTIAL_DATA"));
    }

    @GetMapping("/user-or-admin")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Authenticated user or admin endpoint", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<String>> userOrAdmin() {
        return ResponseEntity.ok(ApiResponse.success("Access granted to authenticated user", "USER_DATA"));
    }
}
