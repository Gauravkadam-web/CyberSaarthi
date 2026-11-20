package org.cybersaarthi.awareness.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.cybersaarthi.awareness.entity.PublicationStatus;

@Schema(description = "Admin request to update topic publication status")
public record UpdateTopicStatusRequest(
    @NotNull(message = "Publication status is required")
    PublicationStatus publicationStatus
) {}
