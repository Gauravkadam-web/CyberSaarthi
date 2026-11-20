package org.cybersaarthi.report.repository;

import org.cybersaarthi.report.entity.ReportStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportStatusHistoryRepository extends JpaRepository<ReportStatusHistory, UUID> {

    List<ReportStatusHistory> findAllByReportIdOrderByChangedAtAsc(UUID reportId);
}
