package com.ohjeon.life_is_egg.domain.dashboard.service;

import com.ohjeon.life_is_egg.domain.auth.entity.User;
import com.ohjeon.life_is_egg.domain.auth.repository.UserRepository;
import com.ohjeon.life_is_egg.domain.dashboard.dto.DashboardStatsResponse;
import com.ohjeon.life_is_egg.domain.dashboard.port.DashboardMetricContributor;
import com.ohjeon.life_is_egg.domain.dashboard.support.DashboardMetricsContext;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final UserRepository userRepository;
    private final List<DashboardMetricContributor> contributors;

    @Cacheable(value = "dashboardStats", key = "#userId")
    public DashboardStatsResponse getStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        LocalDate today = LocalDate.now();
        DashboardMetricsContext context = new DashboardMetricsContext();

        for (DashboardMetricContributor contributor : contributors) {
            contributor.contribute(user, today, context);
        }

        return DashboardStatsResponse.builder()
                .weeklyGoal(context.getWeeklyGoal())
                .monthlyGoal(context.getMonthlyGoal())
                .categoryTime(context.getCategoryTime())
                .activitySummary(new DashboardStatsResponse.ActivitySummary(
                        context.getDiaryCount(), context.getCheerCount()))
                .build();
    }
}