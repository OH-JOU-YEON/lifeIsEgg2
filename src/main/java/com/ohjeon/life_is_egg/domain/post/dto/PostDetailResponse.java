package com.ohjeon.life_is_egg.domain.post.dto;

import com.ohjeon.life_is_egg.domain.post.entity.Post;
import com.ohjeon.life_is_egg.domain.post.entity.Visibility;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class PostDetailResponse {

    private final Long id;
    private final String title;
    private final String content;
    private final String uuid;
    private final Visibility visibility;
    private final boolean isOwner;
    private final int cheerCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public PostDetailResponse(Post post, boolean isOwner) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.uuid = post.getUuid();
        this.visibility = post.getVisibility();
        this.isOwner = isOwner;
        this.cheerCount = 0;
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
    }
}