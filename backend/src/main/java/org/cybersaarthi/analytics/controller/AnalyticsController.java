package org.cybersaarthi.analytics.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.cybersaarthi.analytics.dto.*;
import org.cybersaarthi.analytics.service.AnalyticsService;
import org.cybersaarthi.common.response.ApiResponse;
import org.cybersaarthi.security.user.CustomUserDetails;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Analytics & Research Intelligence", description = "Privileged endpoints for operational KPIs, educational assessment metrics, and privacy-preserving de-identified exports")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/overview")
    @Operation(summary = "Get operational overview metrics", description = "Returns reconciled totals for incidents, financial loss, educational quiz assessments, and ML triage diagnostics", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<AnalyticsOverviewDto>> getOverview() {
        AnalyticsOverviewDto dto = analyticsService.getOverview();
        return ResponseEntity.ok(ApiResponse.success("Analytics overview retrieved successfully", dto));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get category breakdown", description = "Returns incident frequency distribution across approved taxonomy categories with k=5 small-cell masking", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<CategoryBreakdownDto>>> getCategoryBreakdown() {
        List<CategoryBreakdownDto> breakdown = analyticsService.getCategoryBreakdown();
        return ResponseEntity.ok(ApiResponse.success("Category breakdown retrieved successfully", breakdown));
    }

    @GetMapping("/channels")
    @Operation(summary = "Get channel breakdown", description = "Returns incident frequency distribution across digital incident channels with k=5 small-cell masking", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<ChannelBreakdownDto>>> getChannelBreakdown() {
        List<ChannelBreakdownDto> breakdown = analyticsService.getChannelBreakdown();
        return ResponseEntity.ok(ApiResponse.success("Channel breakdown retrieved successfully", breakdown));
    }

    @GetMapping("/assessments")
    @Operation(summary = "Get educational assessment analytics (Track B)", description = "Returns quiz attempt counts, distinct participants, pass rates, and topic-level score metrics", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<AssessmentStatsDto>> getAssessmentStats() {
        AssessmentStatsDto stats = analyticsService.getAssessmentStats();
        return ResponseEntity.ok(ApiResponse.success("Assessment analytics retrieved successfully", stats));
    }

    @GetMapping("/ml-diagnostics")
    @Operation(summary = "Get ML model triage diagnostics", description = "Returns prediction health counts, manual review rate, and average confidence scores", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<MlDiagnosticsDto>> getMlDiagnostics() {
        MlDiagnosticsDto diag = analyticsService.getMlDiagnostics();
        return ResponseEntity.ok(ApiResponse.success("ML diagnostics retrieved successfully", diag));
    }

    @GetMapping("/export/incidents")
    @Operation(summary = "Download de-identified incident dataset (CSV)", description = "Exports research dataset completely stripped of direct personal identifiers with binned financial loss and small-cell privacy guards", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<byte[]> exportIncidentsCsv(@AuthenticationPrincipal CustomUserDetails userDetails) {
        byte[] csvData = analyticsService.exportDeIdentifiedIncidentsCsv(userDetails.getId());
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cybersaarthi_incidents_deidentified.csv\"")
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(csvData);
    }

    @GetMapping("/export/assessments")
    @Operation(summary = "Download de-identified assessment dataset (CSV)", description = "Exports educational quiz performance dataset for Track B academic research and intervention defense", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<byte[]> exportAssessmentsCsv(@AuthenticationPrincipal CustomUserDetails userDetails) {
        byte[] csvData = analyticsService.exportDeIdentifiedAssessmentsCsv(userDetails.getId());
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cybersaarthi_assessments_deidentified.csv\"")
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(csvData);
    }
}
