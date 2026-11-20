package org.cybersaarthi.report.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "review_notes")
public class ReviewNote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private IncidentReport report;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "note_text", nullable = false, columnDefinition = "TEXT")
    private String noteText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NoteVisibility visibility = NoteVisibility.INTERNAL;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public ReviewNote() {
    }

    public ReviewNote(UUID authorId, String noteText, NoteVisibility visibility) {
        this.authorId = authorId;
        this.noteText = noteText;
        this.visibility = visibility;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public IncidentReport getReport() {
        return report;
    }

    public void setReport(IncidentReport report) {
        this.report = report;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public void setAuthorId(UUID authorId) {
        this.authorId = authorId;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public NoteVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(NoteVisibility visibility) {
        this.visibility = visibility;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
