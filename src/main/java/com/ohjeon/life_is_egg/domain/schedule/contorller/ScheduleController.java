package com.ohjeon.life_is_egg.domain.schedule.contorller;

import com.ohjeon.life_is_egg.domain.schedule.dto.ScheduleCreateRequest;
import com.ohjeon.life_is_egg.domain.schedule.dto.ScheduleResponse;
import com.ohjeon.life_is_egg.domain.schedule.service.ScheduleService;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<?> getSchedules(
            @AuthenticationPrincipal Long userId) {
        List<ScheduleResponse> result = scheduleService.getSchedules(userId);
        return ResponseEntity.ok(Map.of("status", 200, "message", "success", "data", result));
    }

    @PostMapping
    public ResponseEntity<?> create(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid ScheduleCreateRequest request) {
        scheduleService.create(userId, request);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", 201);
        response.put("message", "일정이 등록되었습니다");
        response.put("data", null);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{scheduleId}")
    public ResponseEntity<?> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long scheduleId,
            @RequestBody @Valid ScheduleCreateRequest request) {
        scheduleService.update(userId, scheduleId, request);
        return ResponseEntity.ok(Map.of("status", 200, "message", "일정이 수정되었습니다"));
    }

    @PatchMapping("/{scheduleId}/complete")
    public ResponseEntity<?> toggleComplete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long scheduleId) {
        scheduleService.toggleComplete(userId, scheduleId);
        return ResponseEntity.ok(Map.of("status", 200, "message", "일정 완료 상태가 변경되었습니다"));
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<?> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long scheduleId) {
        scheduleService.delete(userId, scheduleId);
        return ResponseEntity.ok(Map.of("status", 200, "message", "일정이 삭제되었습니다"));
    }
}