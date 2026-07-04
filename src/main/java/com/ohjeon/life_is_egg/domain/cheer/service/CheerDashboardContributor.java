package com.ohjeon.life_is_egg.domain.cheer.service;

import com.ohjeon.life_is_egg.domain.auth.entity.User;
import com.ohjeon.life_is_egg.domain.cheer.repository.CheerRepository;
import com.ohjeon.life_is_egg.domain.dashboard.port.DashboardMetricContributor;
import com.ohjeon.life_is_egg.domain.dashboard.support.DashboardMetricsContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheerDashboardContributor implements DashboardMetricContributor {

    private final CheerRepository cheerRepository;

    @Override
    public void contribute(User user, LocalDate today, DashboardMetricsContext context) {
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());
        LocalDateTime monthStartDt = monthStart.atStartOfDay();
        LocalDateTime monthEndDt = monthEnd.atTime(23, 59, 59);

        long cheerCount = cheerRepository.countCheersByPostOwner(user, monthStartDt, monthEndDt);
        context.setCheerCount(cheerCount);
    }
}