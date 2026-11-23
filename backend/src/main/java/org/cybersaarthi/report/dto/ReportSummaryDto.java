package org.cybersaarthi.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.cybersaarthi.report.entity.IncidentChannel;
import org.cybersaarthi.report.entity.ReportStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Summary view of an incident report for listings and tables")
public record ReportSummaryDto(
    UUID id,
    String referenceCode,
    IncidentChannel incidentChannel,
    LocalDate incidentDate,
    ReportStatus currentStatus,
    String userCategoryName,
    String reviewedCategoryName,
    String predictedCategoryName,
    BigDecimal financialLossAmount,
    Instant submittedAt
) {}
