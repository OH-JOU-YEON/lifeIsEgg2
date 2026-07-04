package com.ohjeon.life_is_egg.domain.dashboard.port;


import com.ohjeon.life_is_egg.domain.auth.entity.User;
import com.ohjeon.life_is_egg.domain.dashboard.support.DashboardMetricsContext;
import java.time.LocalDate;

public interface DashboardMetricContributor {

    void contribute(User user, LocalDate today, DashboardMetricsContext context);
}