package org.cybersaarthi.classification.repository;

import org.cybersaarthi.classification.entity.MlPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MlPredictionRepository extends JpaRepository<MlPrediction, UUID> {

    List<MlPrediction> findAllByReportIdOrderByInferredAtDesc(UUID reportId);

    Optional<MlPrediction> findTopByReportIdOrderByInferredAtDesc(UUID reportId);

    @Query("SELECT m.inferenceStatus, COUNT(m) FROM MlPrediction m GROUP BY m.inferenceStatus")
    List<Object[]> countByInferenceStatus();

    long countByRequiresManualReviewTrue();

    @Query("SELECT COALESCE(AVG(m.confidenceScore), 0) FROM MlPrediction m WHERE m.confidenceScore IS NOT NULL AND m.confidenceScore > 0")
    BigDecimal avgConfidenceScore();
}

