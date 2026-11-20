package org.cybersaarthi.awareness.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Admin request payload to author a new awareness educational topic")
public record CreateTopicRequest(

    @Schema(description = "Topic title", example = "UPI & QR Code Fraud Prevention")
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    String title,

    @Schema(description = "Short summary", example = "Crucial preventive tips against QR code and PIN harvesting scams.")
    @NotBlank(message = "Summary is required")
    @Size(max = 500, message = "Summary cannot exceed 500 characters")
    String summary,

    @Schema(description = "Full educational markdown guide content")
    @NotBlank(message = "Body markdown content is required")
    String bodyMarkdown,

    @Schema(description = "Taxonomy v1.0 category code", example = "FINANCIAL_UPI_FRAUD")
    @NotBlank(message = "Topic category is required")
    @Size(max = 80, message = "Topic category code cannot exceed 80 characters")
    String topicCategory,

    @Schema(description = "Optional custom slug (auto-generated from title if blank)", example = "upi-qr-fraud-prevention")
    String slug
) {}
