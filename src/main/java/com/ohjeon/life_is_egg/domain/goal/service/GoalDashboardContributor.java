package com.ohjeon.life_is_egg.domain.goal.service;

import com.ohjeon.life_is_egg.domain.auth.entity.User;
import com.ohjeon.life_is_egg.domain.dashboard.dto.DashboardStatsResponse.GoalStats;
import com.ohjeon.life_is_egg.domain.dashboard.port.DashboardMetricContributor;
import com.ohjeon.life_is_egg.domain.dashboard.support.DashboardMetricsContext;
import com.ohjeon.life_is_egg.domain.goal.repository.GoalRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoalDashboardContributor implements DashboardMetricContributor {

    private final GoalRepository goalRepository;

    @Override
    public void contribute(User user, LocalDate today, DashboardMetricsContext context) {
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = today.with(DayOfWeek.SUNDAY);
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        long weeklyTotal = goalRepository.countByUserAndStartDateBetween(user, weekStart, weekEnd);
        long weeklyCompleted = goalRepository.countByUserAndStartDateBetweenAndCompleted(user, weekStart, weekEnd,
                true);
        double weeklyRate = weeklyTotal == 0 ? 0 : (double) weeklyCompleted / weeklyTotal * 100;

        long monthlyTotal = goalRepository.countByUserAndStartDateBetween(user, monthStart, monthEnd);
        long monthlyCompleted = goalRepository.countByUserAndStartDateBetweenAndCompleted(user, monthStart, monthEnd,
                true);
        double monthlyRate = monthlyTotal == 0 ? 0 : (double) monthlyCompleted / monthlyTotal * 100;

        context.setWeeklyGoal(new GoalStats(weeklyTotal, weeklyCompleted, weeklyRate));
        context.setMonthlyGoal(new GoalStats(monthlyTotal, monthlyCompleted, monthlyRate));
    }
}