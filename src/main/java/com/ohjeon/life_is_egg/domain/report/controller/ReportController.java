package com.ohjeon.life_is_egg.domain.report.controller;

import com.ohjeon.life_is_egg.domain.report.dto.ReportCreateRequest;
import com.ohjeon.life_is_egg.domain.report.service.ReportService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<?> create(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid ReportCreateRequest request) {
        reportService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("status", 201, "message", "신고가 접수되었습니다", "data", null));
    }
}