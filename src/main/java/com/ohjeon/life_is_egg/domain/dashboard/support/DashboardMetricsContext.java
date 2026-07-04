package com.ohjeon.life_is_egg.domain.dashboard.support;

import com.ohjeon.life_is_egg.domain.dashboard.dto.DashboardStatsResponse.CategoryTimeStats;
import com.ohjeon.life_is_egg.domain.dashboard.dto.DashboardStatsResponse.GoalStats;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardMetricsContext {

    private GoalStats weeklyGoal;
    private GoalStats monthlyGoal;
    private List<CategoryTimeStats> categoryTime = new ArrayList<>();
    private long diaryCount;
    private long cheerCount;
}