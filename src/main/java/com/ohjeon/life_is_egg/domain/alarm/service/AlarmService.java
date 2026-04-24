package com.ohjeon.life_is_egg.domain.alarm.service;

import com.ohjeon.life_is_egg.domain.alarm.dto.AlarmResponse;
import com.ohjeon.life_is_egg.domain.alarm.entity.Alarm;
import com.ohjeon.life_is_egg.domain.alarm.repository.AlarmRepository;
import com.ohjeon.life_is_egg.domain.auth.entity.User;
import com.ohjeon.life_is_egg.domain.auth.repository.UserRepository;
import com.ohjeon.life_is_egg.domain.cheer.entity.Cheer;
import com.ohjeon.life_is_egg.domain.post.entity.Post;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final UserRepository userRepository;

    // 알림 목록 조회
    public List<AlarmResponse> getAlarms(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        return alarmRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(AlarmResponse::new)
                .toList();
    }

    // 읽지 않은 알림 개수
    public long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        return alarmRepository.countByUserAndReadFalse(user);
    }

    // 알림 읽음 처리
    @Transactional
    public void readAlarm(Long userId, Long alarmId) {
        Alarm alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다."));

        if (!alarm.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 알림만 읽음 처리할 수 있습니다.");
        }

        alarm.read();
    }

    // 알림 생성 (응원 작성 시 CheerService에서 호출)
    @Transactional
    public void createCheerAlarm(User postOwner, Post post, Cheer cheer) {
        Alarm alarm = Alarm.builder()
                .user(postOwner)
                .post(post)
                .cheer(cheer)
                .content("회원님의 일기에 새 응원이 달렸습니다")
                .build();

        alarmRepository.save(alarm);
    }
}