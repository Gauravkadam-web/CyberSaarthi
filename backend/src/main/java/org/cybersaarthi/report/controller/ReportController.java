package org.cybersaarthi.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.cybersaarthi.common.exception.UnauthorizedException;
import org.cybersaarthi.common.response.ApiResponse;
import org.cybersaarthi.report.dto.CreateReportRequest;
import org.cybersaarthi.report.dto.ReportDetailDto;
import org.cybersaarthi.report.dto.ReportSummaryDto;
import org.cybersaarthi.report.service.ReportService;
import org.cybersaarthi.security.user.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Incident Reporting & Tracking", description = "Endpoints for citizens to document incidents and track review status")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    @Operation(
        summary = "Submit incident report",
        description = "Documents a cyber incident, generates a unique reference code, sets status to SUBMITTED, and records audit trail.",
        security = @SecurityRequirement(name = "BearerAuth")
    )
    public ResponseEntity<ApiResponse<ReportDetailDto>> submitReport(
            @Valid @RequestBody CreateReportRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new UnauthorizedException("You must be logged in to submit an incident report");
        }

        ReportDetailDto report = reportService.submitReport(userDetails.getId(), request);
        return new ResponseEntity<>(ApiResponse.created("Incident report submitted successfully", report), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
        summary = "List authored reports",
        description = "Returns all incident reports submitted by the currently logged-in user.",
        security = @SecurityRequirement(name = "BearerAuth")
    )
    public ResponseEntity<ApiResponse<List<ReportSummaryDto>>> getMyReports(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new UnauthorizedException("You must be logged in to view your reports");
        }

        List<ReportSummaryDto> reports = reportService.getUserReports(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("User reports retrieved successfully", reports));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get report details by ID",
        description = "Returns full incident overview and status history. Enforces strict ownership check (IDOR protected).",
        security = @SecurityRequirement(name = "BearerAuth")
    )
    public ResponseEntity<ApiResponse<ReportDetailDto>> getReportById(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new UnauthorizedException("You must be logged in to view report details");
        }

        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        ReportDetailDto report = reportService.getReportById(id, userDetails.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Report details retrieved successfully", report));
    }

    @GetMapping("/track/{referenceCode}")
    @Operation(
        summary = "Track report by reference code",
        description = "Enables citizens to track their incident by reference code (e.g., CS-2026-A8K2F).",
        security = @SecurityRequirement(name = "BearerAuth")
    )
    public ResponseEntity<ApiResponse<ReportDetailDto>> getReportByReferenceCode(
            @PathVariable String referenceCode,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new UnauthorizedException("You must be logged in to track a report");
        }

        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        ReportDetailDto report = reportService.getReportByReferenceCode(referenceCode, userDetails.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Report tracking details retrieved", report));
    }
}
