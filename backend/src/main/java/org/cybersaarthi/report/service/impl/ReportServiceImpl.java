package org.cybersaarthi.report.service.impl;

import org.cybersaarthi.category.entity.IncidentCategory;
import org.cybersaarthi.category.repository.IncidentCategoryRepository;
import org.cybersaarthi.classification.dto.MlPredictionDto;
import org.cybersaarthi.classification.service.ClassificationService;
import org.cybersaarthi.common.exception.ApiException;
import org.cybersaarthi.common.exception.ForbiddenException;
import org.cybersaarthi.common.exception.ResourceNotFoundException;
import org.cybersaarthi.report.dto.*;
import org.cybersaarthi.report.entity.*;
import org.cybersaarthi.report.repository.AuditEventRepository;
import org.cybersaarthi.report.repository.IncidentReportRepository;
import org.cybersaarthi.report.repository.ReportStatusHistoryRepository;
import org.cybersaarthi.report.repository.ReviewNoteRepository;
import org.cybersaarthi.report.service.ReportService;
import org.cybersaarthi.report.util.ReferenceCodeGenerator;
import org.cybersaarthi.report.util.ReportStateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);

    // Data minimisation regex patterns
    private static final Pattern CARD_PATTERN = Pattern.compile("\\b(?:\\d[ -]*?){13,16}\\b");
    private static final Pattern SENSITIVE_KEYWORD_PATTERN = Pattern.compile(
        "(?i)(atm\\s*pin|cvv\\s*code|card\\s*cvv|netbanking\\s*password|login\\s*password)"
    );

    private final IncidentReportRepository reportRepository;
    private final ReportStatusHistoryRepository statusHistoryRepository;
    private final ReviewNoteRepository reviewNoteRepository;
    private final AuditEventRepository auditEventRepository;
    private final IncidentCategoryRepository categoryRepository;
    private final ReferenceCodeGenerator referenceCodeGenerator;
    private final ReportStateMachine reportStateMachine;
    private final ClassificationService classificationService;

    public ReportServiceImpl(
            IncidentReportRepository reportRepository,
            ReportStatusHistoryRepository statusHistoryRepository,
            ReviewNoteRepository reviewNoteRepository,
            AuditEventRepository auditEventRepository,
            IncidentCategoryRepository categoryRepository,
            ReferenceCodeGenerator referenceCodeGenerator,
            ReportStateMachine reportStateMachine,
            ClassificationService classificationService) {
        this.reportRepository = reportRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.reviewNoteRepository = reviewNoteRepository;
        this.auditEventRepository = auditEventRepository;
        this.categoryRepository = categoryRepository;
        this.referenceCodeGenerator = referenceCodeGenerator;
        this.reportStateMachine = reportStateMachine;
        this.classificationService = classificationService;
    }

    @Override
    @Transactional
    public ReportDetailDto submitReport(UUID userId, CreateReportRequest request) {
        validateDataMinimisation(request.description());

        String referenceCode = generateUniqueReferenceCode();

        IncidentReport report = new IncidentReport(
            referenceCode,
            userId,
            request.incidentChannel(),
            request.incidentDate(),
            request.description().trim(),
            request.financialLossAmount(),
            request.userSelectedCategoryId()
        );

        IncidentReport savedReport = reportRepository.save(report);

        // Record initial status history
        ReportStatusHistory initialHistory = new ReportStatusHistory(
            null,
            ReportStatus.SUBMITTED,
            userId,
            "Initial citizen incident report submission"
        );
        savedReport.addStatusHistory(initialHistory);
        statusHistoryRepository.save(initialHistory);

        // Write immutable audit event
        auditEventRepository.save(new AuditEvent(
            userId,
            "REPORT_SUBMITTED",
            "INCIDENT_REPORT",
            savedReport.getId(),
            String.format("{\"referenceCode\":\"%s\",\"status\":\"SUBMITTED\"}", referenceCode)
        ));

        // Trigger advisory ML classification (resilient failure policy: report submission succeeds even if ML fails)
        try {
            classificationService.classifyAndPersist(savedReport);
        } catch (Exception ex) {
            log.warn("Non-fatal: ML classification could not be completed at submission time: {}", ex.getMessage());
        }

        log.info("Citizen report submitted successfully with reference: {} (ID: {})", referenceCode, savedReport.getId());

        return mapToDetailDto(savedReport, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportSummaryDto> getUserReports(UUID userId) {
        List<IncidentReport> reports = reportRepository.findAllByUserIdOrderBySubmittedAtDesc(userId);
        return reports.stream()
            .map(this::mapToSummaryDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReportDetailDto getReportById(UUID reportId, UUID currentUserId, boolean isAdmin) {
        IncidentReport report = reportRepository.findById(reportId)
            .orElseThrow(() -> new ResourceNotFoundException("IncidentReport", "id", reportId));

        enforceOwnershipOrAdmin(report, currentUserId, isAdmin);
        return mapToDetailDto(report, isAdmin);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportDetailDto getReportByReferenceCode(String refCode, UUID currentUserId, boolean isAdmin) {
        IncidentReport report = reportRepository.findByReferenceCode(refCode.trim().toUpperCase())
            .orElseThrow(() -> new ResourceNotFoundException("IncidentReport", "referenceCode", refCode));

        enforceOwnershipOrAdmin(report, currentUserId, isAdmin);
        return mapToDetailDto(report, isAdmin);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportSummaryDto> getAdminQueue(ReportStatus status, String query, Pageable pageable) {
        return reportRepository.findAdminQueue(status, query, pageable)
            .map(this::mapToSummaryDto);
    }

    @Override
    @Transactional
    public ReportDetailDto updateStatus(UUID reportId, UpdateReportStatusRequest request, UUID adminUserId) {
        IncidentReport report = reportRepository.findById(reportId)
            .orElseThrow(() -> new ResourceNotFoundException("IncidentReport", "id", reportId));

        ReportStatus currentStatus = report.getCurrentStatus();
        ReportStatus newStatus = request.newStatus();

        if (!reportStateMachine.isValidTransition(currentStatus, newStatus)) {
            throw new ApiException(
                String.format("Invalid status transition from %s to %s", currentStatus, newStatus),
                HttpStatus.BAD_REQUEST
            );
        }

        report.setCurrentStatus(newStatus);
        if (request.reviewedCategoryId() != null) {
            report.setReviewedCategoryId(request.reviewedCategoryId());
        }

        IncidentReport updatedReport = reportRepository.save(report);

        // Record history
        ReportStatusHistory history = new ReportStatusHistory(
            currentStatus,
            newStatus,
            adminUserId,
            request.reason().trim()
        );
        updatedReport.addStatusHistory(history);
        statusHistoryRepository.save(history);

        // Record audit event
        auditEventRepository.save(new AuditEvent(
            adminUserId,
            "STATUS_CHANGED",
            "INCIDENT_REPORT",
            updatedReport.getId(),
            String.format("{\"from\":\"%s\",\"to\":\"%s\",\"reason\":\"%s\"}", currentStatus, newStatus, request.reason().trim())
        ));

        log.info("Report ID {} transitioned from {} to {} by admin {}", reportId, currentStatus, newStatus, adminUserId);

        return mapToDetailDto(updatedReport, true);
    }

    @Override
    @Transactional
    public ReviewNoteDto addReviewNote(UUID reportId, AddReviewNoteRequest request, UUID adminUserId) {
        IncidentReport report = reportRepository.findById(reportId)
            .orElseThrow(() -> new ResourceNotFoundException("IncidentReport", "id", reportId));

        ReviewNote note = new ReviewNote(adminUserId, request.noteText().trim(), request.visibility());
        report.addReviewNote(note);
        ReviewNote savedNote = reviewNoteRepository.save(note);

        auditEventRepository.save(new AuditEvent(
            adminUserId,
            "REVIEW_NOTE_ADDED",
            "INCIDENT_REPORT",
            reportId,
            String.format("{\"noteId\":\"%s\",\"visibility\":\"%s\"}", savedNote.getId(), savedNote.getVisibility())
        ));

        log.info("Added review note ID {} to report ID {}", savedNote.getId(), reportId);

        return new ReviewNoteDto(
            savedNote.getId(),
            savedNote.getAuthorId(),
            savedNote.getNoteText(),
            savedNote.getVisibility(),
            savedNote.getCreatedAt()
        );
    }

    private void validateDataMinimisation(String description) {
        if (CARD_PATTERN.matcher(description).find()) {
            throw new ApiException(
                "Data Minimisation Warning: You appear to have entered a 16-digit payment card number. For security, do NOT submit bank card numbers, CVVs, or passwords.",
                HttpStatus.BAD_REQUEST
            );
        }
        if (SENSITIVE_KEYWORD_PATTERN.matcher(description).find()) {
            throw new ApiException(
                "Data Minimisation Warning: Do not disclose PINs, card CVVs, or banking passwords in incident narratives.",
                HttpStatus.BAD_REQUEST
            );
        }
    }

    private void enforceOwnershipOrAdmin(IncidentReport report, UUID currentUserId, boolean isAdmin) {
        if (!isAdmin && !report.getUserId().equals(currentUserId)) {
            throw new ForbiddenException("Access denied: You do not own this incident report.");
        }
    }

    private String generateUniqueReferenceCode() {
        String code;
        int attempts = 0;
        do {
            code = referenceCodeGenerator.generate();
            attempts++;
            if (attempts > 10) {
                throw new ApiException("Failed to generate unique reference code. Please retry.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } while (reportRepository.existsByReferenceCode(code));

        return code;
    }

    private ReportSummaryDto mapToSummaryDto(IncidentReport report) {
        String userCat = resolveCategoryName(report.getUserSelectedCategoryId());
        String reviewedCat = resolveCategoryName(report.getReviewedCategoryId());
        String predictedCat = classificationService.getLatestPredictionForReport(report.getId())
            .map(MlPredictionDto::predictedCategoryName)
            .orElse(null);

        return new ReportSummaryDto(
            report.getId(),
            report.getReferenceCode(),
            report.getIncidentChannel(),
            report.getIncidentDate(),
            report.getCurrentStatus(),
            userCat,
            reviewedCat,
            predictedCat,
            report.getFinancialLossAmount(),
            report.getSubmittedAt()
        );
    }

    private ReportDetailDto mapToDetailDto(IncidentReport report, boolean isAdmin) {
        String userCat = resolveCategoryName(report.getUserSelectedCategoryId());
        String reviewedCat = resolveCategoryName(report.getReviewedCategoryId());

        List<StatusHistoryDto> historyDtos = statusHistoryRepository.findAllByReportIdOrderByChangedAtAsc(report.getId())
            .stream()
            .map(h -> new StatusHistoryDto(h.getId(), h.getFromStatus(), h.getToStatus(), h.getChangeReason(), h.getChangedAt()))
            .collect(Collectors.toList());

        List<ReviewNote> notes = isAdmin
            ? reviewNoteRepository.findAllByReportIdOrderByCreatedAtAsc(report.getId())
            : reviewNoteRepository.findAllByReportIdAndVisibilityOrderByCreatedAtAsc(report.getId(), NoteVisibility.USER_VISIBLE);

        List<ReviewNoteDto> noteDtos = notes.stream()
            .map(n -> new ReviewNoteDto(n.getId(), n.getAuthorId(), n.getNoteText(), n.getVisibility(), n.getCreatedAt()))
            .collect(Collectors.toList());

        MlPredictionDto latestPrediction = classificationService.getLatestPredictionForReport(report.getId())
            .orElse(null);

        return new ReportDetailDto(
            report.getId(),
            report.getReferenceCode(),
            report.getUserId(),
            report.getIncidentChannel(),
            report.getIncidentDate(),
            report.getDescription(),
            report.getFinancialLossAmount(),
            report.getUserSelectedCategoryId(),
            userCat,
            report.getReviewedCategoryId(),
            reviewedCat,
            report.getCurrentStatus(),
            report.getSubmittedAt(),
            report.getUpdatedAt(),
            report.getClosedAt(),
            historyDtos,
            noteDtos,
            latestPrediction
        );
    }

    private String resolveCategoryName(UUID categoryId) {
        if (categoryId == null) return null;
        return categoryRepository.findById(categoryId)
            .map(IncidentCategory::getDisplayName)
            .orElse(null);
    }
}
