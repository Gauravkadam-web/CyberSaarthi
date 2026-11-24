package org.cybersaarthi.analytics.service.impl;

import org.cybersaarthi.analytics.dto.*;
import org.cybersaarthi.analytics.service.AnalyticsService;
import org.cybersaarthi.analytics.util.CsvSanitizer;
import org.cybersaarthi.analytics.util.SmallCellSuppressionHelper;
import org.cybersaarthi.assessment.entity.QuizAttempt;
import org.cybersaarthi.assessment.repository.QuizAttemptRepository;
import org.cybersaarthi.awareness.entity.AwarenessTopic;
import org.cybersaarthi.awareness.repository.AwarenessTopicRepository;
import org.cybersaarthi.category.entity.IncidentCategory;
import org.cybersaarthi.category.repository.IncidentCategoryRepository;
import org.cybersaarthi.classification.entity.MlPrediction;
import org.cybersaarthi.classification.repository.MlPredictionRepository;
import org.cybersaarthi.report.entity.AuditEvent;
import org.cybersaarthi.report.entity.IncidentChannel;
import org.cybersaarthi.report.entity.IncidentReport;
import org.cybersaarthi.report.entity.ReportStatus;
import org.cybersaarthi.report.repository.AuditEventRepository;
import org.cybersaarthi.report.repository.IncidentReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsServiceImpl.class);
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final IncidentReportRepository reportRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final MlPredictionRepository mlPredictionRepository;
    private final IncidentCategoryRepository categoryRepository;
    private final AwarenessTopicRepository topicRepository;
    private final AuditEventRepository auditEventRepository;

    public AnalyticsServiceImpl(
            IncidentReportRepository reportRepository,
            QuizAttemptRepository quizAttemptRepository,
            MlPredictionRepository mlPredictionRepository,
            IncidentCategoryRepository categoryRepository,
            AwarenessTopicRepository topicRepository,
            AuditEventRepository auditEventRepository) {
        this.reportRepository = reportRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.mlPredictionRepository = mlPredictionRepository;
        this.categoryRepository = categoryRepository;
        this.topicRepository = topicRepository;
        this.auditEventRepository = auditEventRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AnalyticsOverviewDto getOverview() {
        long totalIncidents = reportRepository.count();
        long submitted = reportRepository.countByCurrentStatus(ReportStatus.SUBMITTED);
        long underReview = reportRepository.countByCurrentStatus(ReportStatus.UNDER_REVIEW);
        long needsInfo = reportRepository.countByCurrentStatus(ReportStatus.NEEDS_INFORMATION);
        long reviewed = reportRepository.countByCurrentStatus(ReportStatus.REVIEWED);
        long closed = reportRepository.countByCurrentStatus(ReportStatus.CLOSED);

        BigDecimal totalLoss = reportRepository.sumFinancialLoss();
        BigDecimal avgLoss = reportRepository.avgFinancialLoss();

        long totalAttempts = quizAttemptRepository.count();
        long distinctParticipants = quizAttemptRepository.countDistinctParticipants();
        long passedAttempts = quizAttemptRepository.countPassedAttempts();
        double passRate = totalAttempts > 0 ? (double) passedAttempts / totalAttempts * 100.0 : 0.0;
        double avgScore = quizAttemptRepository.avgScorePercentage();

        long totalMl = mlPredictionRepository.count();
        long manualReviewCount = mlPredictionRepository.countByRequiresManualReviewTrue();
        BigDecimal avgMlConfidence = mlPredictionRepository.avgConfidenceScore();

        return new AnalyticsOverviewDto(
            totalIncidents,
            submitted,
            underReview,
            needsInfo,
            reviewed,
            closed,
            totalLoss,
            avgLoss.setScale(2, RoundingMode.HALF_UP),
            totalAttempts,
            distinctParticipants,
            Math.round(passRate * 10.0) / 10.0,
            Math.round(avgScore * 10.0) / 10.0,
            totalMl,
            manualReviewCount,
            avgMlConfidence.setScale(4, RoundingMode.HALF_UP)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryBreakdownDto> getCategoryBreakdown() {
        long total = reportRepository.count();
        List<Object[]> rawCounts = reportRepository.countByEffectiveCategory();

        Map<UUID, Long> countMap = new HashMap<>();
        for (Object[] row : rawCounts) {
            UUID catId = (UUID) row[0];
            Long cnt = ((Number) row[1]).longValue();
            countMap.put(catId, cnt);
        }

        List<IncidentCategory> allCategories = categoryRepository.findAllByTaxonomyVersionAndIsActiveTrue("1.0");
        List<CategoryBreakdownDto> result = new ArrayList<>();

        for (IncidentCategory cat : allCategories) {
            long c = countMap.getOrDefault(cat.getId(), 0L);
            double pct = total > 0 ? (double) c / total * 100.0 : 0.0;
            result.add(new CategoryBreakdownDto(
                cat.getCode(),
                cat.getDisplayName(),
                c,
                SmallCellSuppressionHelper.maskSmallCell(c),
                Math.round(pct * 10.0) / 10.0
            ));
        }

        // Handle unclassified / null category
        long unclassifiedCount = countMap.getOrDefault(null, 0L);
        if (unclassifiedCount > 0) {
            double unclassPct = total > 0 ? (double) unclassifiedCount / total * 100.0 : 0.0;
            result.add(new CategoryBreakdownDto(
                "OTHER_UNCLASSIFIED",
                "Unclassified / Pending Triage",
                unclassifiedCount,
                SmallCellSuppressionHelper.maskSmallCell(unclassifiedCount),
                Math.round(unclassPct * 10.0) / 10.0
            ));
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChannelBreakdownDto> getChannelBreakdown() {
        long total = reportRepository.count();
        List<Object[]> rawCounts = reportRepository.countByIncidentChannel();

        Map<IncidentChannel, Long> countMap = new HashMap<>();
        for (Object[] row : rawCounts) {
            IncidentChannel channel = (IncidentChannel) row[0];
            Long cnt = ((Number) row[1]).longValue();
            countMap.put(channel, cnt);
        }

        List<ChannelBreakdownDto> result = new ArrayList<>();
        for (IncidentChannel channel : IncidentChannel.values()) {
            long c = countMap.getOrDefault(channel, 0L);
            double pct = total > 0 ? (double) c / total * 100.0 : 0.0;
            result.add(new ChannelBreakdownDto(
                channel.name(),
                formatChannelName(channel),
                c,
                SmallCellSuppressionHelper.maskSmallCell(c),
                Math.round(pct * 10.0) / 10.0
            ));
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public AssessmentStatsDto getAssessmentStats() {
        long totalAttempts = quizAttemptRepository.count();
        long distinctParticipants = quizAttemptRepository.countDistinctParticipants();
        long passedAttempts = quizAttemptRepository.countPassedAttempts();
        double passRate = totalAttempts > 0 ? (double) passedAttempts / totalAttempts * 100.0 : 0.0;
        double avgScore = quizAttemptRepository.avgScorePercentage();

        List<Object[]> topicAggs = quizAttemptRepository.findTopicAggregates();
        List<TopicAggregateDto> topicDtos = new ArrayList<>();

        for (Object[] row : topicAggs) {
            UUID topicId = (UUID) row[0];
            long count = ((Number) row[1]).longValue();
            double score = ((Number) row[2]).doubleValue();

            Optional<AwarenessTopic> topicOpt = topicRepository.findById(topicId);
            String title = topicOpt.map(AwarenessTopic::getTitle).orElse("Unknown Topic");
            String cat = topicOpt.map(AwarenessTopic::getTopicCategory).orElse("GENERAL");

            topicDtos.add(new TopicAggregateDto(
                topicId,
                title,
                cat,
                count,
                Math.round(score * 10.0) / 10.0
            ));
        }

        return new AssessmentStatsDto(
            totalAttempts,
            distinctParticipants,
            passedAttempts,
            Math.round(passRate * 10.0) / 10.0,
            Math.round(avgScore * 10.0) / 10.0,
            topicDtos
        );
    }

    @Override
    @Transactional(readOnly = true)
    public MlDiagnosticsDto getMlDiagnostics() {
        long total = mlPredictionRepository.count();
        long manualReview = mlPredictionRepository.countByRequiresManualReviewTrue();
        BigDecimal avgConf = mlPredictionRepository.avgConfidenceScore();

        Map<String, Long> statusMap = new HashMap<>();
        List<Object[]> rows = mlPredictionRepository.countByInferenceStatus();
        for (Object[] row : rows) {
            statusMap.put(row[0].toString(), ((Number) row[1]).longValue());
        }

        return new MlDiagnosticsDto(
            total,
            manualReview,
            avgConf.setScale(4, RoundingMode.HALF_UP),
            statusMap
        );
    }

    @Override
    @Transactional
    public byte[] exportDeIdentifiedIncidentsCsv(UUID adminUserId) {
        List<IncidentReport> reports = reportRepository.findAllForExport();

        // Map categories for lookup
        Map<UUID, String> categoryCodeMap = categoryRepository.findAll().stream()
            .collect(Collectors.toMap(IncidentCategory::getId, IncidentCategory::getCode, (a, b) -> a));

        StringBuilder sb = new StringBuilder();
        // CSV Header (strictly zero direct personal identifiers)
        sb.append("Pseudo_Record_ID,Incident_Channel,Incident_YearMonth,Financial_Loss_Bracket,Category_Code,Status,ML_Predicted_Category,ML_Inference_Status,ML_Confidence_Band\n");

        int counter = 1;
        for (IncidentReport r : reports) {
            String pseudoId = String.format("R-%05d", counter++);
            String channel = r.getIncidentChannel() != null ? r.getIncidentChannel().name() : "UNKNOWN";
            String yearMonth = r.getIncidentDate() != null ? r.getIncidentDate().format(YEAR_MONTH_FORMATTER) : "UNKNOWN";
            String lossBracket = classifyLossBracket(r.getFinancialLossAmount());

            UUID effectiveCatId = r.getReviewedCategoryId() != null ? r.getReviewedCategoryId() : r.getUserSelectedCategoryId();
            String catCode = effectiveCatId != null ? categoryCodeMap.getOrDefault(effectiveCatId, "UNKNOWN") : "UNCLASSIFIED";

            String status = r.getCurrentStatus() != null ? r.getCurrentStatus().name() : "SUBMITTED";

            // Latest ML prediction metadata if available
            Optional<MlPrediction> predOpt = mlPredictionRepository.findTopByReportIdOrderByInferredAtDesc(r.getId());
            String mlCategory = predOpt.map(MlPrediction::getPredictedCategoryCode).orElse("NONE");
            String mlStatus = predOpt.map(p -> p.getInferenceStatus().name()).orElse("NONE");
            String confBand = predOpt.map(p -> classifyConfidenceBand(p.getConfidenceScore())).orElse("NONE");

            sb.append(CsvSanitizer.escapeCsvCell(pseudoId)).append(",")
              .append(CsvSanitizer.escapeCsvCell(channel)).append(",")
              .append(CsvSanitizer.escapeCsvCell(yearMonth)).append(",")
              .append(CsvSanitizer.escapeCsvCell(lossBracket)).append(",")
              .append(CsvSanitizer.escapeCsvCell(catCode)).append(",")
              .append(CsvSanitizer.escapeCsvCell(status)).append(",")
              .append(CsvSanitizer.escapeCsvCell(mlCategory)).append(",")
              .append(CsvSanitizer.escapeCsvCell(mlStatus)).append(",")
              .append(CsvSanitizer.escapeCsvCell(confBand)).append("\n");
        }

        // Record audit event for research export
        UUID exportId = UUID.randomUUID();
        auditEventRepository.save(new AuditEvent(
            adminUserId,
            "RESEARCH_DATA_EXPORTED",
            "INCIDENT_REPORTS",
            exportId,
            String.format("{\"type\":\"INCIDENTS_CSV\",\"recordCount\":%d}", reports.size())
        ));

        log.info("Admin {} exported de-identified incidents CSV ({} records)", adminUserId, reports.size());
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    @Transactional
    public byte[] exportDeIdentifiedAssessmentsCsv(UUID adminUserId) {
        List<QuizAttempt> attempts = quizAttemptRepository.findAllForExport();

        Map<UUID, String> topicTitleMap = topicRepository.findAll().stream()
            .collect(Collectors.toMap(AwarenessTopic::getId, AwarenessTopic::getTitle, (a, b) -> a));

        StringBuilder sb = new StringBuilder();
        sb.append("Pseudo_Attempt_ID,Topic_Title,Attempt_YearMonth,Score_Percentage,Passed\n");

        int counter = 1;
        for (QuizAttempt qa : attempts) {
            String pseudoId = String.format("QA-%05d", counter++);
            String title = topicTitleMap.getOrDefault(qa.getTopicId(), "General Quiz");
            String yearMonth = qa.getSubmittedAt() != null ? DateTimeFormatter.ofPattern("yyyy-MM").withZone(java.time.ZoneOffset.UTC).format(qa.getSubmittedAt()) : "UNKNOWN";
            double score = qa.getScorePercentage();
            boolean passed = score >= 70.0;

            sb.append(CsvSanitizer.escapeCsvCell(pseudoId)).append(",")
              .append(CsvSanitizer.escapeCsvCell(title)).append(",")
              .append(CsvSanitizer.escapeCsvCell(yearMonth)).append(",")
              .append(String.format("%.1f", score)).append(",")
              .append(passed ? "YES" : "NO").append("\n");
        }

        UUID exportId = UUID.randomUUID();
        auditEventRepository.save(new AuditEvent(
            adminUserId,
            "RESEARCH_DATA_EXPORTED",
            "QUIZ_ATTEMPTS",
            exportId,
            String.format("{\"type\":\"ASSESSMENTS_CSV\",\"recordCount\":%d}", attempts.size())
        ));

        log.info("Admin {} exported de-identified assessments CSV ({} records)", adminUserId, attempts.size());
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String classifyLossBracket(BigDecimal loss) {
        if (loss == null || loss.compareTo(BigDecimal.ZERO) == 0) {
            return "ZERO_LOSS";
        }
        if (loss.compareTo(new BigDecimal("1000")) <= 0) {
            return "INR_1_TO_1000";
        }
        if (loss.compareTo(new BigDecimal("5000")) <= 0) {
            return "INR_1001_TO_5000";
        }
        if (loss.compareTo(new BigDecimal("25000")) <= 0) {
            return "INR_5001_TO_25000";
        }
        return "INR_ABOVE_25000";
    }

    private String classifyConfidenceBand(BigDecimal conf) {
        if (conf == null || conf.compareTo(BigDecimal.ZERO) == 0) {
            return "FALLBACK_OR_ZERO";
        }
        if (conf.compareTo(new BigDecimal("0.80")) >= 0) {
            return "HIGH_CONFIDENCE (>=80%)";
        }
        if (conf.compareTo(new BigDecimal("0.60")) >= 0) {
            return "MODERATE_CONFIDENCE (60-79%)";
        }
        return "LOW_CONFIDENCE (<60%)";
    }

    private String formatChannelName(IncidentChannel channel) {
        return switch (channel) {
            case UPI_APP -> "UPI / Payment App";
            case WHATSAPP -> "WhatsApp Messaging";
            case PHONE_CALL -> "Phone / Voice Call";
            case SMS -> "SMS Smishing";
            case EMAIL -> "Email Phishing";
            case WEBSITE_PORTAL -> "Fake Website / Portal";
            case SOCIAL_MEDIA -> "Social Media";
            case OTHER -> "Other Digital Channel";
        };
    }
}
