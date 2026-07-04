package com.ohjeon.life_is_egg.domain.post.service;

import com.ohjeon.life_is_egg.domain.auth.entity.User;
import com.ohjeon.life_is_egg.domain.dashboard.port.DashboardMetricContributor;
import com.ohjeon.life_is_egg.domain.dashboard.support.DashboardMetricsContext;
import com.ohjeon.life_is_egg.domain.post.repository.PostRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostDashboardContributor implements DashboardMetricContributor {

    private final PostRepository postRepository;

    @Override
    public void contribute(User user, LocalDate today, DashboardMetricsContext context) {
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());
        LocalDateTime monthStartDt = monthStart.atStartOfDay();
        LocalDateTime monthEndDt = monthEnd.atTime(23, 59, 59);

        long diaryCount = postRepository.countByUserAndCreatedAtBetweenAndDeletedFalse(user, monthStartDt, monthEndDt);
        context.setDiaryCount(diaryCount);
    }
}