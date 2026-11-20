package org.cybersaarthi.report.util;

import org.cybersaarthi.report.entity.ReportStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class ReportStateMachine {

    private static final Map<ReportStatus, Set<ReportStatus>> ALLOWED_TRANSITIONS = Map.of(
        ReportStatus.SUBMITTED, Set.of(ReportStatus.UNDER_REVIEW),
        ReportStatus.UNDER_REVIEW, Set.of(ReportStatus.NEEDS_INFORMATION, ReportStatus.REVIEWED, ReportStatus.CLOSED),
        ReportStatus.NEEDS_INFORMATION, Set.of(ReportStatus.UNDER_REVIEW),
        ReportStatus.REVIEWED, Set.of(ReportStatus.CLOSED),
        ReportStatus.CLOSED, Set.of() // Terminal state
    );

    public boolean isValidTransition(ReportStatus currentStatus, ReportStatus newStatus) {
        if (currentStatus == null || newStatus == null || currentStatus == newStatus) {
            return false;
        }

        Set<ReportStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        return allowed.contains(newStatus);
    }
}
