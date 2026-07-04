package com.ohjeon.life_is_egg.domain.cheer.event;

import java.util.List;

public record CheerCreatedEvent(
        List<CheerAlarmTarget> targets,
        Long postId,
        String postUuid,
        Long cheerId
) {
}