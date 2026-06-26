package com.ohjeon.life_is_egg.domain.dashboard.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsResponse {

    private GoalStats weeklyGoal;
    private GoalStats monthlyGoal;
    private List<CategoryTimeStats> categoryTime;
    private ActivitySummary activitySummary;

    public record GoalStats(long totalCount, long completedCount, double achievementRate) {
    }

    public record CategoryTimeStats(String category, double totalHours) {
    }

    public record ActivitySummary(long diaryCount, long cheerCount) {
    }
}