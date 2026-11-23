package org.cybersaarthi.classification.repository;

import org.cybersaarthi.classification.entity.MlPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MlPredictionRepository extends JpaRepository<MlPrediction, UUID> {

    List<MlPrediction> findAllByReportIdOrderByInferredAtDesc(UUID reportId);

    Optional<MlPrediction> findTopByReportIdOrderByInferredAtDesc(UUID reportId);
}
