package com.ohjeon.life_is_egg.domain.dashboard.controller;

import com.ohjeon.life_is_egg.domain.dashboard.dto.DashboardStatsResponse;
import com.ohjeon.life_is_egg.domain.dashboard.service.DashboardService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(
            @AuthenticationPrincipal Long userId) {
        DashboardStatsResponse result = dashboardService.getStats(userId);
        return ResponseEntity.ok(Map.of("status", 200, "message", "success", "data", result));
    }
}