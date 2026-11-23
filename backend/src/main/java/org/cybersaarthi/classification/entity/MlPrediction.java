package org.cybersaarthi.classification.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ml_predictions")
public class MlPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "report_id", nullable = false)
    private UUID reportId;

    @Column(name = "predicted_category_id")
    private UUID predictedCategoryId;

    @Column(name = "predicted_category_code", nullable = false, length = 80)
    private String predictedCategoryCode;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Column(name = "model_version", nullable = false, length = 80)
    private String modelVersion;

    @Column(name = "taxonomy_version", nullable = false, length = 30)
    private String taxonomyVersion;

    @Column(name = "confidence_score", precision = 5, scale = 4)
    private BigDecimal confidenceScore;

    @Column(name = "score_type", nullable = false, length = 50)
    private String scoreType;

    @Column(name = "requires_manual_review", nullable = false)
    private boolean requiresManualReview;

    @Enumerated(EnumType.STRING)
    @Column(name = "inference_status", nullable = false, length = 40)
    private InferenceStatus inferenceStatus;

    @Column(name = "inferred_at", nullable = false, updatable = false)
    private Instant inferredAt;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    public MlPrediction() {
        this.inferredAt = Instant.now();
    }

    public MlPrediction(
            UUID reportId,
            UUID predictedCategoryId,
            String predictedCategoryCode,
            String modelName,
            String modelVersion,
            String taxonomyVersion,
            BigDecimal confidenceScore,
            String scoreType,
            boolean requiresManualReview,
            InferenceStatus inferenceStatus,
            String errorMessage) {
        this.reportId = reportId;
        this.predictedCategoryId = predictedCategoryId;
        this.predictedCategoryCode = predictedCategoryCode;
        this.modelName = modelName;
        this.modelVersion = modelVersion;
        this.taxonomyVersion = taxonomyVersion;
        this.confidenceScore = confidenceScore;
        this.scoreType = scoreType;
        this.requiresManualReview = requiresManualReview;
        this.inferenceStatus = inferenceStatus;
        this.errorMessage = errorMessage;
        this.inferredAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getReportId() {
        return reportId;
    }

    public void setReportId(UUID reportId) {
        this.reportId = reportId;
    }

    public UUID getPredictedCategoryId() {
        return predictedCategoryId;
    }

    public void setPredictedCategoryId(UUID predictedCategoryId) {
        this.predictedCategoryId = predictedCategoryId;
    }

    public String getPredictedCategoryCode() {
        return predictedCategoryCode;
    }

    public void setPredictedCategoryCode(String predictedCategoryCode) {
        this.predictedCategoryCode = predictedCategoryCode;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getTaxonomyVersion() {
        return taxonomyVersion;
    }

    public void setTaxonomyVersion(String taxonomyVersion) {
        this.taxonomyVersion = taxonomyVersion;
    }

    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(BigDecimal confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getScoreType() {
        return scoreType;
    }

    public void setScoreType(String scoreType) {
        this.scoreType = scoreType;
    }

    public boolean isRequiresManualReview() {
        return requiresManualReview;
    }

    public void setRequiresManualReview(boolean requiresManualReview) {
        this.requiresManualReview = requiresManualReview;
    }

    public InferenceStatus getInferenceStatus() {
        return inferenceStatus;
    }

    public void setInferenceStatus(InferenceStatus inferenceStatus) {
        this.inferenceStatus = inferenceStatus;
    }

    public Instant getInferredAt() {
        return inferredAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
