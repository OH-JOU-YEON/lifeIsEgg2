package com.ohjeon.life_is_egg.domain.schedule.repository;

import com.ohjeon.life_is_egg.domain.auth.entity.User;
import com.ohjeon.life_is_egg.domain.schedule.entity.Schedule;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByUserOrderByStartTimeAsc(User user);

    @Query(value = """
            SELECT s.category, SUM(TIMESTAMPDIFF(SECOND, s.start_time, s.end_time)) / 3600.0
            FROM schedules s
            WHERE s.user_id = :userId
            AND s.completed = true
            AND s.start_time >= :since
            GROUP BY s.category
            """, nativeQuery = true)
    List<Object[]> findCategoryTimeSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}