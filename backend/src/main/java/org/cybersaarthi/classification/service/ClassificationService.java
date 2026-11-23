package org.cybersaarthi.classification.service;

import org.cybersaarthi.classification.dto.MlPredictionDto;
import org.cybersaarthi.report.entity.IncidentReport;

import java.util.Optional;
import java.util.UUID;

public interface ClassificationService {

    MlPredictionDto classifyAndPersist(IncidentReport report);

    Optional<MlPredictionDto> getLatestPredictionForReport(UUID reportId);

    MlPredictionDto reclassifyReport(UUID reportId, UUID adminUserId);
}
