package org.cybersaarthi.classification.service.impl;

import org.cybersaarthi.category.entity.IncidentCategory;
import org.cybersaarthi.category.repository.IncidentCategoryRepository;
import org.cybersaarthi.classification.client.MlInferenceClient;
import org.cybersaarthi.classification.dto.MlClassifyResponse;
import org.cybersaarthi.classification.dto.MlInferenceResult;
import org.cybersaarthi.classification.dto.MlPredictionDto;
import org.cybersaarthi.classification.entity.InferenceStatus;
import org.cybersaarthi.classification.entity.MlPrediction;
import org.cybersaarthi.classification.repository.MlPredictionRepository;
import org.cybersaarthi.classification.service.ClassificationService;
import org.cybersaarthi.common.exception.ResourceNotFoundException;
import org.cybersaarthi.report.entity.AuditEvent;
import org.cybersaarthi.report.entity.IncidentReport;
import org.cybersaarthi.report.repository.AuditEventRepository;
import org.cybersaarthi.report.repository.IncidentReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClassificationServiceImpl implements ClassificationService {

    private static final Logger log = LoggerFactory.getLogger(ClassificationServiceImpl.class);

    private final MlInferenceClient mlInferenceClient;
    private final MlPredictionRepository mlPredictionRepository;
    private final IncidentCategoryRepository categoryRepository;
    private final IncidentReportRepository reportRepository;
    private final AuditEventRepository auditEventRepository;
    private final String taxonomyVersion;

    public ClassificationServiceImpl(
            MlInferenceClient mlInferenceClient,
            MlPredictionRepository mlPredictionRepository,
            IncidentCategoryRepository categoryRepository,
            IncidentReportRepository reportRepository,
            AuditEventRepository auditEventRepository,
            @Value("${cybersaarthi.ml.taxonomy-version:1.0}") String taxonomyVersion) {
        this.mlInferenceClient = mlInferenceClient;
        this.mlPredictionRepository = mlPredictionRepository;
        this.categoryRepository = categoryRepository;
        this.reportRepository = reportRepository;
        this.auditEventRepository = auditEventRepository;
        this.taxonomyVersion = taxonomyVersion;
    }

    @Override
    @Transactional
    public MlPredictionDto classifyAndPersist(IncidentReport report) {
        MlInferenceResult result = mlInferenceClient.classify(report.getDescription(), taxonomyVersion);

        MlPrediction prediction;
        if (result.status() == InferenceStatus.SUCCESS || result.status() == InferenceStatus.LOW_CONFIDENCE) {
            MlClassifyResponse resp = result.response();
            String catCode = resp.predictedCategory();
            Optional<IncidentCategory> catOpt = categoryRepository.findByTaxonomyVersionAndCode(taxonomyVersion, catCode);

            UUID categoryId = catOpt.map(IncidentCategory::getId).orElse(null);
            BigDecimal confScore = resp.confidenceScore() != null
                ? BigDecimal.valueOf(resp.confidenceScore()).setScale(4, RoundingMode.HALF_UP)
                : null;

            boolean manualReview = Boolean.TRUE.equals(resp.requiresManualReview()) || result.status() == InferenceStatus.LOW_CONFIDENCE;

            prediction = new MlPrediction(
                report.getId(),
                categoryId,
                catCode,
                "LogisticRegression-TFIDF",
                resp.modelVersion() != null ? resp.modelVersion() : "baseline-v1.0",
                resp.taxonomyVersion() != null ? resp.taxonomyVersion() : taxonomyVersion,
                confScore,
                resp.scoreType() != null ? resp.scoreType() : "predict_proba",
                manualReview,
                result.status(),
                null
            );
        } else {
            // Offline / timeout / service unavailable fallback
            prediction = new MlPrediction(
                report.getId(),
                null,
                "OTHER_UNCLASSIFIED",
                "LogisticRegression-TFIDF",
                "baseline-v1.0",
                taxonomyVersion,
                BigDecimal.ZERO,
                "fallback",
                true,
                result.status(),
                result.errorMessage()
            );
        }

        MlPrediction saved = mlPredictionRepository.save(prediction);

        // Record immutable audit event
        auditEventRepository.save(new AuditEvent(
            report.getUserId(),
            "ML_INFERENCE_EXECUTED",
            "INCIDENT_REPORT",
            report.getId(),
            String.format("{\"predictionId\":\"%s\",\"status\":\"%s\",\"category\":\"%s\",\"requiresManualReview\":%b}",
                saved.getId(), saved.getInferenceStatus(), saved.getPredictedCategoryCode(), saved.isRequiresManualReview())
        ));

        log.info("ML prediction recorded for report ID {} (status: {}, category: {})",
            report.getId(), saved.getInferenceStatus(), saved.getPredictedCategoryCode());

        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MlPredictionDto> getLatestPredictionForReport(UUID reportId) {
        return mlPredictionRepository.findTopByReportIdOrderByInferredAtDesc(reportId)
            .map(this::mapToDto);
    }

    @Override
    @Transactional
    public MlPredictionDto reclassifyReport(UUID reportId, UUID adminUserId) {
        IncidentReport report = reportRepository.findById(reportId)
            .orElseThrow(() -> new ResourceNotFoundException("IncidentReport", "id", reportId));

        MlPredictionDto dto = classifyAndPersist(report);

        auditEventRepository.save(new AuditEvent(
            adminUserId,
            "ML_RECLASSIFICATION_TRIGGERED",
            "INCIDENT_REPORT",
            reportId,
            String.format("{\"newPredictionId\":\"%s\",\"status\":\"%s\"}", dto.id(), dto.inferenceStatus())
        ));

        log.info("Admin {} re-triggered ML inference for report ID {}", adminUserId, reportId);

        return dto;
    }

    private MlPredictionDto mapToDto(MlPrediction prediction) {
        String categoryName = null;
        if (prediction.getPredictedCategoryId() != null) {
            categoryName = categoryRepository.findById(prediction.getPredictedCategoryId())
                .map(IncidentCategory::getDisplayName)
                .orElse(prediction.getPredictedCategoryCode());
        } else {
            categoryName = prediction.getPredictedCategoryCode();
        }

        return new MlPredictionDto(
            prediction.getId(),
            prediction.getReportId(),
            prediction.getPredictedCategoryId(),
            prediction.getPredictedCategoryCode(),
            categoryName,
            prediction.getModelName(),
            prediction.getModelVersion(),
            prediction.getTaxonomyVersion(),
            prediction.getConfidenceScore(),
            prediction.getScoreType(),
            prediction.isRequiresManualReview(),
            prediction.getInferenceStatus(),
            prediction.getInferredAt(),
            true // isAdvisory: Model suggestions are always advisory
        );
    }
}
