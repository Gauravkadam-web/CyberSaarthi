package org.cybersaarthi.report.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "incident_reports")
public class IncidentReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "reference_code", nullable = false, unique = true, length = 32)
    private String referenceCode;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "incident_channel", nullable = false, length = 80)
    private IncidentChannel incidentChannel;

    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "financial_loss_amount", precision = 12, scale = 2)
    private BigDecimal financialLossAmount;

    @Column(name = "user_selected_category_id")
    private UUID userSelectedCategoryId;

    @Column(name = "reviewed_category_id")
    private UUID reviewedCategoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false, length = 40)
    private ReportStatus currentStatus = ReportStatus.SUBMITTED;

    @Column(name = "disclaimer_acknowledged", nullable = false)
    private boolean disclaimerAcknowledged = true;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("changedAt ASC")
    private List<ReportStatusHistory> statusHistory = new ArrayList<>();

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private List<ReviewNote> reviewNotes = new ArrayList<>();

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private Instant submittedAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "closed_at")
    private Instant closedAt;

    public IncidentReport() {
    }

    public IncidentReport(String referenceCode, UUID userId, IncidentChannel incidentChannel,
                          LocalDate incidentDate, String description, BigDecimal financialLossAmount,
                          UUID userSelectedCategoryId) {
        this.referenceCode = referenceCode;
        this.userId = userId;
        this.incidentChannel = incidentChannel;
        this.incidentDate = incidentDate;
        this.description = description;
        this.financialLossAmount = financialLossAmount;
        this.userSelectedCategoryId = userSelectedCategoryId;
        this.currentStatus = ReportStatus.SUBMITTED;
        this.disclaimerAcknowledged = true;
        this.submittedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public void addStatusHistory(ReportStatusHistory history) {
        this.statusHistory.add(history);
        history.setReport(this);
    }

    public void addReviewNote(ReviewNote note) {
        this.reviewNotes.add(note);
        note.setReport(this);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getReferenceCode() {
        return referenceCode;
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public IncidentChannel getIncidentChannel() {
        return incidentChannel;
    }

    public void setIncidentChannel(IncidentChannel incidentChannel) {
        this.incidentChannel = incidentChannel;
    }

    public LocalDate getIncidentDate() {
        return incidentDate;
    }

    public void setIncidentDate(LocalDate incidentDate) {
        this.incidentDate = incidentDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getFinancialLossAmount() {
        return financialLossAmount;
    }

    public void setFinancialLossAmount(BigDecimal financialLossAmount) {
        this.financialLossAmount = financialLossAmount;
    }

    public UUID getUserSelectedCategoryId() {
        return userSelectedCategoryId;
    }

    public void setUserSelectedCategoryId(UUID userSelectedCategoryId) {
        this.userSelectedCategoryId = userSelectedCategoryId;
    }

    public UUID getReviewedCategoryId() {
        return reviewedCategoryId;
    }

    public void setReviewedCategoryId(UUID reviewedCategoryId) {
        this.reviewedCategoryId = reviewedCategoryId;
    }

    public ReportStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(ReportStatus currentStatus) {
        this.currentStatus = currentStatus;
        if (currentStatus == ReportStatus.CLOSED && this.closedAt == null) {
            this.closedAt = Instant.now();
        }
    }

    public boolean isDisclaimerAcknowledged() {
        return disclaimerAcknowledged;
    }

    public void setDisclaimerAcknowledged(boolean disclaimerAcknowledged) {
        this.disclaimerAcknowledged = disclaimerAcknowledged;
    }

    public List<ReportStatusHistory> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(List<ReportStatusHistory> statusHistory) {
        this.statusHistory = statusHistory;
    }

    public List<ReviewNote> getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(List<ReviewNote> reviewNotes) {
        this.reviewNotes = reviewNotes;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }
}
