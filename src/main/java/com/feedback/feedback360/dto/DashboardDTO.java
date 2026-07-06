package com.feedback.feedback360.dto;

import java.util.List;
import java.util.Map;

public record DashboardDTO(
    long totalCompletions,
    long totalSubmitted,
    double responseRatePercent,
    double averageRatingOverall,
    List<ModuleRating> topModules,
    List<ModuleRating> bottomModules,
    Map<String, Long> byDepartment,
    List<TrendPoint> ratingTrend,
    long pendingCount
) {
    public record ModuleRating(String title, double avgRating, long responses) {}
    public record TrendPoint(String period, double avgRating) {}
}
