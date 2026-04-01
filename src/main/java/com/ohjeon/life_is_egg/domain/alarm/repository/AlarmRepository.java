package com.ohjeon.life_is_egg.domain.alarm.repository;

import com.ohjeon.life_is_egg.domain.alarm.entity.Alarm;
import com.ohjeon.life_is_egg.domain.auth.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    // 알림 목록 조회 (최신순)
    List<Alarm> findByUserOrderByCreatedAtDesc(User user);

    // 읽지 않은 알림 개수
    long countByUserAndReadFalse(User user);
}