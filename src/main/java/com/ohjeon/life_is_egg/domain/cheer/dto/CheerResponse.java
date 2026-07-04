package com.ohjeon.life_is_egg.domain.cheer.dto;

import com.ohjeon.life_is_egg.domain.cheer.entity.Cheer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class CheerResponse {

    private static final String DELETED_CONTENT = "삭제된 응원입니다";

    private final Long id;
    private final String content;
    private final boolean deleted;
    private final Long parentId;
    private final List<CheerResponse> children;
    private final LocalDateTime createdAt;

    public CheerResponse(Cheer cheer) {
        this.id = cheer.getId();
        this.deleted = cheer.isDeleted();
        this.content = cheer.isDeleted() ? DELETED_CONTENT : cheer.getContent();
        this.parentId = cheer.getParent() != null ? cheer.getParent().getId() : null;
        this.children = new ArrayList<>();
        this.createdAt = cheer.getCreatedAt();
    }

    public void addChild(CheerResponse child) {
        this.children.add(child);
    }
}