package org.cybersaarthi.analytics.service;

import org.cybersaarthi.analytics.dto.*;

import java.util.List;
import java.util.UUID;

public interface AnalyticsService {

    AnalyticsOverviewDto getOverview();

    List<CategoryBreakdownDto> getCategoryBreakdown();

    List<ChannelBreakdownDto> getChannelBreakdown();

    AssessmentStatsDto getAssessmentStats();

    MlDiagnosticsDto getMlDiagnostics();

    byte[] exportDeIdentifiedIncidentsCsv(UUID adminUserId);

    byte[] exportDeIdentifiedAssessmentsCsv(UUID adminUserId);
}
