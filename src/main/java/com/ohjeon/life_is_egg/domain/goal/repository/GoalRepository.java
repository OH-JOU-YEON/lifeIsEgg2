package com.ohjeon.life_is_egg.domain.goal.repository;

import com.ohjeon.life_is_egg.domain.auth.entity.User;
import com.ohjeon.life_is_egg.domain.goal.entity.Goal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findByUserAndCompletedOrderByEndDateAsc(User user, boolean completed);

    // 기간 내 전체 목표 수
    long countByUserAndStartDateBetween(User user, LocalDate start, LocalDate end);

    // 기간 내 완료된 목표 수
    long countByUserAndStartDateBetweenAndCompleted(User user, LocalDate start, LocalDate end, boolean completed);
}