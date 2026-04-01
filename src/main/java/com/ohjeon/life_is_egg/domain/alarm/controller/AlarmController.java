package com.ohjeon.life_is_egg.domain.alarm.controller;

import com.ohjeon.life_is_egg.domain.alarm.dto.AlarmResponse;
import com.ohjeon.life_is_egg.domain.alarm.service.AlarmService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    // 읽지 않은 알림 개수
    @GetMapping("/count")
    public ResponseEntity<?> getUnreadCount(
            @AuthenticationPrincipal Long userId) {
        long count = alarmService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("status", 200, "message", "success",
                "data", Map.of("count", count)));
    }

    // 알림 목록 조회
    @GetMapping
    public ResponseEntity<?> getAlarms(
            @AuthenticationPrincipal Long userId) {
        List<AlarmResponse> result = alarmService.getAlarms(userId);
        return ResponseEntity.ok(Map.of("status", 200, "message", "success", "data", result));
    }

    // 알림 읽음 처리
    @PatchMapping("/{alarmId}/read")
    public ResponseEntity<?> readAlarm(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long alarmId) {
        alarmService.readAlarm(userId, alarmId);
        return ResponseEntity.ok(Map.of("status", 200, "message", "읽음 처리되었습니다", "data", null));
    }
}