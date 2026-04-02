package com.ohjeon.life_is_egg.domain.goal.dto;

import com.ohjeon.life_is_egg.domain.goal.entity.Goal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.Getter;

@Getter
public class GoalResponse {

    private final Long id;
    private final String title;
    private final int targetValue;
    private final int currentValue;
    private final String unit;
    private final String category;
    private final double progressRate;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final long dDay;
    private final boolean completed;
    private final LocalDateTime createdAt;

    public GoalResponse(Goal goal) {
        this.id = goal.getId();
        this.title = goal.getTitle();
        this.targetValue = goal.getTargetValue();
        this.currentValue = goal.getCurrentValue();
        this.unit = goal.getUnit();
        this.category = goal.getCategory();
        this.progressRate = (double) goal.getCurrentValue() / goal.getTargetValue() * 100;
        this.startDate = goal.getStartDate();
        this.endDate = goal.getEndDate();
        this.dDay = ChronoUnit.DAYS.between(LocalDate.now(), goal.getEndDate());
        this.completed = goal.isCompleted();
        this.createdAt = goal.getCreatedAt();
    }
}
