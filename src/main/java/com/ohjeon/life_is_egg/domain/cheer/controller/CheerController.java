package com.ohjeon.life_is_egg.domain.cheer.controller;

import com.ohjeon.life_is_egg.domain.cheer.dto.CheerCreateRequest;
import com.ohjeon.life_is_egg.domain.cheer.dto.CheerResponse;
import com.ohjeon.life_is_egg.domain.cheer.service.CheerService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts/{postUuid}/cheers")
@RequiredArgsConstructor
public class CheerController {

    private final CheerService cheerService;

    // 응원 목록 조회
    @GetMapping
    public ResponseEntity<?> getCheers(
            @AuthenticationPrincipal Long userId,
            @PathVariable String postUuid) {
        List<CheerResponse> result = cheerService.getCheers(postUuid);
        return ResponseEntity.ok(Map.of("status", 200, "message", "success", "data", result));
    }

    // 응원 작성
    @PostMapping
    public ResponseEntity<?> create(
            @AuthenticationPrincipal Long userId,
            @PathVariable String postUuid,
            @RequestBody @Valid CheerCreateRequest request) {
        cheerService.create(userId, postUuid, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("status", 201, "message", "응원이 등록되었습니다", "data", null));
    }
}