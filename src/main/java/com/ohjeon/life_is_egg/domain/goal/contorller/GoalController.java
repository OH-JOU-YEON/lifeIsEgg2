package com.ohjeon.life_is_egg.domain.goal.contorller;

import com.ohjeon.life_is_egg.domain.goal.dto.GoalCreateRequest;
import com.ohjeon.life_is_egg.domain.goal.dto.GoalResponse;
import com.ohjeon.life_is_egg.domain.goal.service.GoalService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @GetMapping
    public ResponseEntity<?> getGoals(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "false") boolean completed) {
        List<GoalResponse> result = goalService.getGoals(userId, completed);
        return ResponseEntity.ok(Map.of("status", 200, "message", "success", "data", result));
    }

    @PostMapping
    public ResponseEntity<?> create(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid GoalCreateRequest request) {
        goalService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("status", 201, "message", "목표가 등록되었습니다"));
    }

    @PatchMapping("/{goalId}/progress")
    public ResponseEntity<?> updateProgress(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long goalId,
            @RequestParam int increment) {
        GoalResponse result = goalService.updateProgress(userId, goalId, increment);
        return ResponseEntity.ok(Map.of("status", 200, "message", "진행도가 업데이트되었습니다", "data", result));
    }

    @PutMapping("/{goalId}")
    public ResponseEntity<?> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long goalId,
            @RequestBody @Valid GoalCreateRequest request) {
        goalService.update(userId, goalId, request);
        return ResponseEntity.ok(Map.of("status", 200, "message", "목표가 수정되었습니다"));
    }

    @DeleteMapping("/{goalId}")
    public ResponseEntity<?> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long goalId) {
        goalService.delete(userId, goalId);
        return ResponseEntity.ok(Map.of("status", 200, "message", "목표가 삭제되었습니다"));
    }
}