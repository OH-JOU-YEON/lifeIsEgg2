package com.ohjeon.life_is_egg.domain.dashboard.service;

import com.ohjeon.life_is_egg.domain.auth.entity.User;
import com.ohjeon.life_is_egg.domain.auth.repository.UserRepository;
import com.ohjeon.life_is_egg.domain.cheer.repository.CheerRepository;
import com.ohjeon.life_is_egg.domain.dashboard.dto.DashboardStatsResponse;
import com.ohjeon.life_is_egg.domain.goal.repository.GoalRepository;
import com.ohjeon.life_is_egg.domain.post.repository.PostRepository;
import com.ohjeon.life_is_egg.domain.schedule.repository.ScheduleRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final GoalRepository goalRepository;
    private final ScheduleRepository scheduleRepository;
    private final PostRepository postRepository;
    private final CheerRepository cheerRepository;
    private final UserRepository userRepository;

    public DashboardStatsResponse getStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // 이번 주 범위
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = today.with(DayOfWeek.SUNDAY);

        // 이번 달 범위
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        // 이번 주 목표 달성률
        long weeklyTotal = goalRepository.countByUserAndStartDateBetween(user, weekStart, weekEnd);
        long weeklyCompleted = goalRepository.countByUserAndStartDateBetweenAndCompleted(user, weekStart, weekEnd,
                true);
        double weeklyRate = weeklyTotal == 0 ? 0 : (double) weeklyCompleted / weeklyTotal * 100;

        // 이번 달 목표 달성률
        long monthlyTotal = goalRepository.countByUserAndStartDateBetween(user, monthStart, monthEnd);
        long monthlyCompleted = goalRepository.countByUserAndStartDateBetweenAndCompleted(user, monthStart, monthEnd,
                true);
        double monthlyRate = monthlyTotal == 0 ? 0 : (double) monthlyCompleted / monthlyTotal * 100;

        // 최근 7일 카테고리별 학습 시간
        LocalDateTime since = now.minusDays(7);
        List<Object[]> categoryTimeRaw = scheduleRepository.findCategoryTimeSince(userId, since);
        List<DashboardStatsResponse.CategoryTimeStats> categoryTime = categoryTimeRaw.stream()
                .map(row -> new DashboardStatsResponse.CategoryTimeStats(
                        (String) row[0],
                        ((Number) row[1]).doubleValue()
                ))
                .toList();

        // 이번 달 활동 요약
        LocalDateTime monthStartDt = monthStart.atStartOfDay();
        LocalDateTime monthEndDt = monthEnd.atTime(23, 59, 59);
        long diaryCount = postRepository.countByUserAndCreatedAtBetweenAndDeletedFalse(user, monthStartDt, monthEndDt);
        long cheerCount = cheerRepository.countCheersByPostOwner(user, monthStartDt, monthEndDt);

        return DashboardStatsResponse.builder()
                .weeklyGoal(new DashboardStatsResponse.GoalStats(weeklyTotal, weeklyCompleted, weeklyRate))
                .monthlyGoal(new DashboardStatsResponse.GoalStats(monthlyTotal, monthlyCompleted, monthlyRate))
                .categoryTime(categoryTime)
                .activitySummary(new DashboardStatsResponse.ActivitySummary(diaryCount, cheerCount))
                .build();
    }
}