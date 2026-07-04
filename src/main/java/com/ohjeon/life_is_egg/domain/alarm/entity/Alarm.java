package com.ohjeon.life_is_egg.domain.alarm.entity;

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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "alarms")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Alarm {

    private static final String POST_CHEER_MESSAGE = "회원님의 일기에 새 응원이 달렸습니다";
    private static final String REPLY_CHEER_MESSAGE = "회원님의 응원에 답글이 달렸습니다";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Long postId;

    @Column(nullable = false, length = 36)
    private String postUuid;

    @Column(nullable = false)
    private Long cheerId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Alarm(User user, Long postId, String postUuid, Long cheerId, String content) {
        this.user = user;
        this.postId = postId;
        this.postUuid = postUuid;
        this.cheerId = cheerId;
        this.content = content;
    }

    public static Alarm forPostCheer(User user, Long postId, String postUuid, Long cheerId) {
        return Alarm.builder()
                .user(user)
                .postId(postId)
                .postUuid(postUuid)
                .cheerId(cheerId)
                .content(POST_CHEER_MESSAGE)
                .build();
    }

    public static Alarm forReplyCheer(User user, Long postId, String postUuid, Long cheerId) {
        return Alarm.builder()
                .user(user)
                .postId(postId)
                .postUuid(postUuid)
                .cheerId(cheerId)
                .content(REPLY_CHEER_MESSAGE)
                .build();
    }

    public void read() {
        this.read = true;
    }
}