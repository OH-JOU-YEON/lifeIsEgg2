package com.ohjeon.life_is_egg.domain.post.dto;

import com.ohjeon.life_is_egg.domain.post.entity.Post;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class PostFeedResponse {

    private final Long id;
    private final String title;
    private final String contentPreview;
    private final String uuid;
    private final int cheerCount;
    private final LocalDateTime createdAt;

    public PostFeedResponse(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.contentPreview = post.getContent().length() > 50
                ? post.getContent().substring(0, 50)
                : post.getContent();
        this.uuid = post.getUuid();
        this.cheerCount = 0;
        this.createdAt = post.getCreatedAt();
    }
}