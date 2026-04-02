package com.ohjeon.life_is_egg.domain.goal.entity;

import com.ohjeon.life_is_egg.domain.auth.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "goals")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private int targetValue;

    @Column(nullable = false)
    private int currentValue = 0;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(length = 50)
    private String category;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private boolean completed = false;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Goal(User user, String title, int targetValue, String unit,
                String category, LocalDate startDate, LocalDate endDate) {
        this.user = user;
        this.title = title;
        this.targetValue = targetValue;
        this.unit = unit;
        this.category = category;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void update(String title, int targetValue, LocalDate endDate, String category) {
        this.title = title;
        this.targetValue = targetValue;
        this.endDate = endDate;
        this.category = category;
    }

    public void updateProgress(int increment) {
        this.currentValue = Math.max(0, this.currentValue + increment);
        if (this.currentValue >= this.targetValue) {
            this.completed = true;
        }
    }
}