package org.cybersaarthi.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.cybersaarthi.report.entity.NoteVisibility;

@Schema(description = "Staff review note submission")
public record AddReviewNoteRequest(

    @NotBlank(message = "Note content is required")
    @Size(min = 2, max = 2000, message = "Note text must be between 2 and 2000 characters")
    String noteText,

    @NotNull(message = "Note visibility (INTERNAL or USER_VISIBLE) is required")
    NoteVisibility visibility
) {}
