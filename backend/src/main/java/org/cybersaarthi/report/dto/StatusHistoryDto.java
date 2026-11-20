package org.cybersaarthi.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.cybersaarthi.report.entity.ReportStatus;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Milestone in report status tracking history")
public record StatusHistoryDto(
    UUID id,
    ReportStatus fromStatus,
    ReportStatus toStatus,
    String changeReason,
    Instant changedAt
) {}
