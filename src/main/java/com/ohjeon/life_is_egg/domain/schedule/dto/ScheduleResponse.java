package com.ohjeon.life_is_egg.domain.schedule.dto;

import com.ohjeon.life_is_egg.domain.schedule.entity.Schedule;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class ScheduleResponse {

    private final Long id;
    private final String title;
    private final String description;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String category;
    private final boolean completed;
    private final LocalDateTime createdAt;

    public ScheduleResponse(Schedule schedule) {
        this.id = schedule.getId();
        this.title = schedule.getTitle();
        this.description = schedule.getDescription();
        this.startTime = schedule.getStartTime();
        this.endTime = schedule.getEndTime();
        this.category = schedule.getCategory();
        this.completed = schedule.isCompleted();
        this.createdAt = schedule.getCreatedAt();
    }
}
