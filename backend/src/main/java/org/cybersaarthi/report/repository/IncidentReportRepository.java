package org.cybersaarthi.report.repository;

import org.cybersaarthi.report.entity.IncidentReport;
import org.cybersaarthi.report.entity.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IncidentReportRepository extends JpaRepository<IncidentReport, UUID> {

    Optional<IncidentReport> findByReferenceCode(String referenceCode);

    boolean existsByReferenceCode(String referenceCode);

    List<IncidentReport> findAllByUserIdOrderBySubmittedAtDesc(UUID userId);

    @Query("SELECT r FROM IncidentReport r WHERE " +
           "(:status IS NULL OR r.currentStatus = :status) AND " +
           "(:query IS NULL OR LOWER(r.referenceCode) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(r.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<IncidentReport> findAdminQueue(
            @Param("status") ReportStatus status,
            @Param("query") String query,
            Pageable pageable
    );
}
