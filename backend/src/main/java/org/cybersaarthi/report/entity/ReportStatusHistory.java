package org.cybersaarthi.report.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "report_status_history")
public class ReportStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private IncidentReport report;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 40)
    private ReportStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 40)
    private ReportStatus toStatus;

    @Column(name = "changed_by", nullable = false)
    private UUID changedBy;

    @Column(name = "change_reason", columnDefinition = "TEXT")
    private String changeReason;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt = Instant.now();

    public ReportStatusHistory() {
    }

    public ReportStatusHistory(ReportStatus fromStatus, ReportStatus toStatus, UUID changedBy, String changeReason) {
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedBy = changedBy;
        this.changeReason = changeReason;
        this.changedAt = Instant.now();
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

    public ReportStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(ReportStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public ReportStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(ReportStatus toStatus) {
        this.toStatus = toStatus;
    }

    public UUID getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(UUID changedBy) {
        this.changedBy = changedBy;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Instant changedAt) {
        this.changedAt = changedAt;
    }
}
