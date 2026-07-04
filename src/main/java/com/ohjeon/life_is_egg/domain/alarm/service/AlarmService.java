package com.ohjeon.life_is_egg.domain.alarm.service;

import com.ohjeon.life_is_egg.domain.alarm.dto.AlarmResponse;
import com.ohjeon.life_is_egg.domain.alarm.entity.Alarm;
import com.ohjeon.life_is_egg.domain.alarm.repository.AlarmRepository;
import com.ohjeon.life_is_egg.domain.auth.entity.User;
import com.ohjeon.life_is_egg.domain.auth.repository.UserRepository;
import com.ohjeon.life_is_egg.domain.cheer.event.CheerAlarmTarget;
import com.ohjeon.life_is_egg.domain.cheer.event.CheerCreatedEvent;
import com.ohjeon.life_is_egg.domain.cheer.event.CheerDeletedEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final UserRepository userRepository;

    public List<AlarmResponse> getAlarms(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        return alarmRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(AlarmResponse::new)
                .toList();
    }

    public long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        return alarmRepository.countByUserAndReadFalse(user);
    }

    @Transactional
    public void readAlarm(Long userId, Long alarmId) {
        Alarm alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다."));

        if (!alarm.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 알림만 읽음 처리할 수 있습니다.");
        }

        alarm.read();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCheerCreated(CheerCreatedEvent event) {
        for (CheerAlarmTarget target : event.targets()) {
            User user = userRepository.findById(target.recipientUserId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

            Alarm alarm = switch (target.type()) {
                case POST_CHEER -> Alarm.forPostCheer(user, event.postId(), event.postUuid(), event.cheerId());
                case REPLY_CHEER -> Alarm.forReplyCheer(user, event.postId(), event.postUuid(), event.cheerId());
            };

            alarmRepository.save(alarm);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCheerDeleted(CheerDeletedEvent event) {
        alarmRepository.deleteByCheerId(event.cheerId());
    }
}