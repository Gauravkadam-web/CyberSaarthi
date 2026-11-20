package org.cybersaarthi.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.cybersaarthi.report.entity.ReportStatus;

import java.util.UUID;

@Schema(description = "Admin request payload to transition report status")
public record UpdateReportStatusRequest(

    @NotNull(message = "New status is required")
    ReportStatus newStatus,

    @NotBlank(message = "Transition reason is mandatory for audit trail")
    @Size(min = 5, max = 500, message = "Reason must be between 5 and 500 characters")
    String reason,

    @Schema(description = "Confirmed category ID if transitioning to REVIEWED (optional)")
    UUID reviewedCategoryId
) {}
