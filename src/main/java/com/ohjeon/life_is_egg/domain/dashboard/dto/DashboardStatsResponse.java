package com.ohjeon.life_is_egg.domain.dashboard.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardStatsResponse {

    private final GoalStats weeklyGoal;
    private final GoalStats monthlyGoal;
    private final List<CategoryTimeStats> categoryTime;
    private final ActivitySummary activitySummary;


    public record GoalStats(long totalCount, long completedCount, double achievementRate) {
    }


    public record CategoryTimeStats(String category, double totalHours) {
    }


    public record ActivitySummary(long diaryCount, long cheerCount) {
    }
}