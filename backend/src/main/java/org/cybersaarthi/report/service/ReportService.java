package org.cybersaarthi.report.service;

import org.cybersaarthi.report.dto.*;
import org.cybersaarthi.report.entity.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ReportService {

    ReportDetailDto submitReport(UUID userId, CreateReportRequest request);

    List<ReportSummaryDto> getUserReports(UUID userId);

    ReportDetailDto getReportById(UUID reportId, UUID currentUserId, boolean isAdmin);

    ReportDetailDto getReportByReferenceCode(String refCode, UUID currentUserId, boolean isAdmin);

    Page<ReportSummaryDto> getAdminQueue(ReportStatus status, String query, Pageable pageable);

    ReportDetailDto updateStatus(UUID reportId, UpdateReportStatusRequest request, UUID adminUserId);

    ReviewNoteDto addReviewNote(UUID reportId, AddReviewNoteRequest request, UUID adminUserId);
}
