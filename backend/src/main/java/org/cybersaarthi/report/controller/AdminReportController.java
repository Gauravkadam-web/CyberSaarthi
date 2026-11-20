package org.cybersaarthi.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.cybersaarthi.common.response.ApiResponse;
import org.cybersaarthi.report.dto.*;
import org.cybersaarthi.report.entity.ReportStatus;
import org.cybersaarthi.report.service.ReportService;
import org.cybersaarthi.security.user.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Report Review Queue", description = "Privileged administrator endpoints to review, triage, update status, and attach review notes")
public class AdminReportController {

    private final ReportService reportService;

    public AdminReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    @Operation(
        summary = "Get admin review queue",
        description = "Returns paginated incident reports with optional filtering by status and text search.",
        security = @SecurityRequirement(name = "BearerAuth")
    )
    public ResponseEntity<ApiResponse<Page<ReportSummaryDto>>> getAdminQueue(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) String query,
            @PageableDefault(size = 10, sort = "submittedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ReportSummaryDto> queue = reportService.getAdminQueue(status, query, pageable);
        return ResponseEntity.ok(ApiResponse.success("Admin review queue retrieved successfully", queue));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get full report detail (Admin)",
        description = "Returns full incident overview including internal review notes, state transition history, and audit log.",
        security = @SecurityRequirement(name = "BearerAuth")
    )
    public ResponseEntity<ApiResponse<ReportDetailDto>> getReportDetail(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ReportDetailDto report = reportService.getReportById(id, userDetails.getId(), true);
        return ResponseEntity.ok(ApiResponse.success("Report details retrieved successfully", report));
    }

    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Update report review status",
        description = "Transitions report status adhering to state machine constraints. Records audit event and transition history.",
        security = @SecurityRequirement(name = "BearerAuth")
    )
    public ResponseEntity<ApiResponse<ReportDetailDto>> updateReportStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateReportStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ReportDetailDto updated = reportService.updateStatus(id, request, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Report status updated successfully", updated));
    }

    @PostMapping("/{id}/notes")
    @Operation(
        summary = "Attach review note",
        description = "Adds an administrative review note to an incident report, either INTERNAL or USER_VISIBLE.",
        security = @SecurityRequirement(name = "BearerAuth")
    )
    public ResponseEntity<ApiResponse<ReviewNoteDto>> addReviewNote(
            @PathVariable UUID id,
            @Valid @RequestBody AddReviewNoteRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ReviewNoteDto note = reportService.addReviewNote(id, request, userDetails.getId());
        return new ResponseEntity<>(ApiResponse.created("Review note added successfully", note), HttpStatus.CREATED);
    }
}
