package org.cybersaarthi.report.repository;

import org.cybersaarthi.report.entity.NoteVisibility;
import org.cybersaarthi.report.entity.ReviewNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewNoteRepository extends JpaRepository<ReviewNote, UUID> {

    List<ReviewNote> findAllByReportIdOrderByCreatedAtAsc(UUID reportId);

    List<ReviewNote> findAllByReportIdAndVisibilityOrderByCreatedAtAsc(UUID reportId, NoteVisibility visibility);
}
