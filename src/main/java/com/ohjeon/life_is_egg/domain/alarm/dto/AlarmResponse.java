package com.ohjeon.life_is_egg.domain.alarm.dto;

import com.ohjeon.life_is_egg.domain.alarm.entity.Alarm;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class AlarmResponse {

    private final Long id;
    private final String postUuid;
    private final String content;
    private final Long postId;
    private final Long cheerId;
    private final boolean isRead;
    private final LocalDateTime createdAt;

    public AlarmResponse(Alarm alarm) {
        this.id = alarm.getId();
        this.postUuid = alarm.getPost() != null ? alarm.getPost().getUuid() : null;
        this.content = alarm.getContent();
        this.postId = alarm.getPost() != null ? alarm.getPost().getId() : null;
        this.cheerId = alarm.getCheer() != null ? alarm.getCheer().getId() : null;
        this.isRead = alarm.isRead();
        this.createdAt = alarm.getCreatedAt();
    }
}