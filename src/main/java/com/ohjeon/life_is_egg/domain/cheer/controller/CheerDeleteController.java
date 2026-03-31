package com.ohjeon.life_is_egg.domain.cheer.controller;

import com.ohjeon.life_is_egg.domain.cheer.service.CheerService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cheers")
@RequiredArgsConstructor
public class CheerDeleteController {

    private final CheerService cheerService;

    // 응원 삭제
    @DeleteMapping("/{cheerId}")
    public ResponseEntity<?> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long cheerId) {
        cheerService.delete(userId, cheerId);
        return ResponseEntity.ok(Map.of("status", 200, "message", "응원이 삭제되었습니다", "data", null));
    }
}