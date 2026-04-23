package com.ohjeon.life_is_egg.domain.post.controller;

import com.ohjeon.life_is_egg.domain.post.dto.PostCreateRequest;
import com.ohjeon.life_is_egg.domain.post.dto.PostDetailResponse;
import com.ohjeon.life_is_egg.domain.post.dto.PostFeedResponse;
import com.ohjeon.life_is_egg.domain.post.dto.PostMyResponse;
import com.ohjeon.life_is_egg.domain.post.service.PostService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 일기 작성
    @PostMapping
    public ResponseEntity<?> create(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid PostCreateRequest request) {
        postService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("status", 201, "message", "일기가 작성되었습니다"));
    }

    // 내 일기 목록
    @GetMapping("/my")
    public ResponseEntity<?> getMyPosts(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostMyResponse> result = postService.getMyPosts(userId, pageable);
        return ResponseEntity.ok(Map.of("status", 200, "message", "success", "data", result));
    }

    // 또래 피드
    @GetMapping("/feed")
    public ResponseEntity<?> getFeed(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false, defaultValue = "") List<Long> excludeIds) {
        List<PostFeedResponse> result = postService.getFeed(userId, excludeIds);
        return ResponseEntity.ok(Map.of("status", 200, "message", "success", "data", result));
    }

    // 일기 상세
    @GetMapping("/{uuid}")
    public ResponseEntity<?> getPost(
            @AuthenticationPrincipal Long userId,
            @PathVariable String uuid) {
        PostDetailResponse result = postService.getPost(userId, uuid);
        return ResponseEntity.ok(Map.of("status", 200, "message", "success", "data", result));
    }

    // 일기 수정
    @PutMapping("/{uuid}")
    public ResponseEntity<?> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable String uuid,
            @RequestBody @Valid PostCreateRequest request) {
        postService.update(userId, uuid, request);
        return ResponseEntity.ok(Map.of("status", 200, "message", "일기가 수정되었습니다", "data", null));
    }

    // 일기 삭제
    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable String uuid) {
        postService.delete(userId, uuid);
        return ResponseEntity.ok(Map.of("status", 200, "message", "일기가 삭제되었습니다", "data", null));
    }
}