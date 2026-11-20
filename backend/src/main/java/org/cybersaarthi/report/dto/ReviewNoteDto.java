package org.cybersaarthi.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.cybersaarthi.report.entity.NoteVisibility;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Staff review note on an incident report")
public record ReviewNoteDto(
    UUID id,
    UUID authorId,
    String noteText,
    NoteVisibility visibility,
    Instant createdAt
) {}
