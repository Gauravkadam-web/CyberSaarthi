package org.cybersaarthi.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.cybersaarthi.classification.dto.MlPredictionDto;
import org.cybersaarthi.report.entity.IncidentChannel;
import org.cybersaarthi.report.entity.ReportStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(description = "Complete detail view of an incident report including tracking timeline and ML advisory prediction")
public record ReportDetailDto(
    UUID id,
    String referenceCode,
    UUID userId,
    IncidentChannel incidentChannel,
    LocalDate incidentDate,
    String description,
    BigDecimal financialLossAmount,
    UUID userSelectedCategoryId,
    String userCategoryName,
    UUID reviewedCategoryId,
    String reviewedCategoryName,
    ReportStatus currentStatus,
    Instant submittedAt,
    Instant updatedAt,
    Instant closedAt,
    List<StatusHistoryDto> statusHistory,
    List<ReviewNoteDto> reviewNotes,
    MlPredictionDto latestPrediction
) {}
