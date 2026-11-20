package org.cybersaarthi.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.cybersaarthi.report.entity.IncidentChannel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Citizen incident report submission payload")
public record CreateReportRequest(

    @NotNull(message = "Incident channel is required")
    @Schema(description = "Channel through which incident took place", example = "UPI_APP")
    IncidentChannel incidentChannel,

    @NotNull(message = "Incident date is required")
    @PastOrPresent(message = "Incident date cannot be in the future")
    @Schema(description = "Date when incident occurred", example = "2026-11-20")
    LocalDate incidentDate,

    @NotBlank(message = "Incident description is required")
    @Size(min = 20, max = 5000, message = "Description must be between 20 and 5000 characters")
    @Schema(description = "Detailed factual narrative of the incident (DO NOT include bank passwords, PINs, or card CVVs)", example = "Received a call claiming to be electricity department asking to scan QR code to avoid power disconnection.")
    String description,

    @DecimalMin(value = "0.0", inclusive = true, message = "Financial loss amount cannot be negative")
    @Schema(description = "Approximate financial loss in INR (optional)", example = "5000.00")
    BigDecimal financialLossAmount,

    @Schema(description = "User-suspected incident category ID (optional)")
    UUID userSelectedCategoryId,

    @AssertTrue(message = "You must acknowledge that CyberSaarthi is an educational support portal and not an official police authority")
    @Schema(description = "Mandatory disclaimer acknowledgement", example = "true")
    boolean disclaimerAcknowledged
) {}
