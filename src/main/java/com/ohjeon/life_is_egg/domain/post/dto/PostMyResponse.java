package com.ohjeon.life_is_egg.domain.post.dto;

import com.ohjeon.life_is_egg.domain.post.entity.Post;
import com.ohjeon.life_is_egg.domain.post.entity.Visibility;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class PostMyResponse {

    private final Long id;
    private final String title;
    private final String contentPreview;
    private final Visibility visibility;
    private final int cheerCount;
    private final LocalDateTime createdAt;

    public PostMyResponse(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.contentPreview = post.getContent().length() > 50
                ? post.getContent().substring(0, 50)
                : post.getContent();
        this.visibility = post.getVisibility();
        this.cheerCount = 0; // 응원 기능 구현 후 연결
        this.createdAt = post.getCreatedAt();
    }
}