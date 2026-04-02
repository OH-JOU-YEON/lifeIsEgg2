package com.ohjeon.life_is_egg.domain.schedule.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ohjeon.life_is_egg.domain.auth.entity.User;
import com.ohjeon.life_is_egg.domain.auth.repository.UserRepository;
import com.ohjeon.life_is_egg.domain.schedule.dto.ScheduleCreateRequest;
import com.ohjeon.life_is_egg.domain.schedule.repository.ScheduleRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
class ScheduleServiceTest {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        scheduleRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .email("test2@test.com")
                .password("password1234")
                .nickname("테스터2")
                .age((byte) 26)
                .build());
    }

    @Test
    void 일정_등록_성공() {
        ScheduleCreateRequest request = new ScheduleCreateRequest();
        request.setTitle("알고리즘 공부");
        request.setStartTime(LocalDateTime.of(2026, 4, 2, 14, 0));
        request.setEndTime(LocalDateTime.of(2026, 4, 2, 16, 0));
        request.setCategory("알고리즘");

        scheduleService.create(user.getId(), request);

        assertEquals(1, scheduleRepository.findAll().size());
    }

    @Test
    void 종료시간이_시작시간보다_이전이면_실패() {
        ScheduleCreateRequest request = new ScheduleCreateRequest();
        request.setTitle("알고리즘 공부");
        request.setStartTime(LocalDateTime.of(2026, 4, 2, 16, 0));
        request.setEndTime(LocalDateTime.of(2026, 4, 2, 14, 0));
        request.setCategory("알고리즘");

        assertThrows(IllegalArgumentException.class,
                () -> scheduleService.create(user.getId(), request));
    }

    @Test
    void 일정_완료_토글() {
        ScheduleCreateRequest request = new ScheduleCreateRequest();
        request.setTitle("알고리즘 공부");
        request.setStartTime(LocalDateTime.of(2026, 4, 2, 14, 0));
        request.setEndTime(LocalDateTime.of(2026, 4, 2, 16, 0));
        request.setCategory("알고리즘");
        scheduleService.create(user.getId(), request);

        Long scheduleId = scheduleRepository.findAll().get(0).getId();
        scheduleService.toggleComplete(user.getId(), scheduleId);

        assertTrue(scheduleRepository.findAll().get(0).isCompleted());
    }

    @Test
    void 일정_수정_성공() {
        ScheduleCreateRequest request = new ScheduleCreateRequest();
        request.setTitle("알고리즘 공부");
        request.setStartTime(LocalDateTime.of(2026, 4, 2, 14, 0));
        request.setEndTime(LocalDateTime.of(2026, 4, 2, 16, 0));
        request.setCategory("알고리즘");
        scheduleService.create(user.getId(), request);

        Long scheduleId = scheduleRepository.findAll().get(0).getId();

        ScheduleCreateRequest updateRequest = new ScheduleCreateRequest();
        updateRequest.setTitle("BFS 공부");
        updateRequest.setStartTime(LocalDateTime.of(2026, 4, 2, 15, 0));
        updateRequest.setEndTime(LocalDateTime.of(2026, 4, 2, 17, 0));
        updateRequest.setCategory("알고리즘");
        scheduleService.update(user.getId(), scheduleId, updateRequest);

        assertEquals("BFS 공부", scheduleRepository.findAll().get(0).getTitle());
    }

    @Test
    void 일정_삭제_성공() {
        ScheduleCreateRequest request = new ScheduleCreateRequest();
        request.setTitle("알고리즘 공부");
        request.setStartTime(LocalDateTime.of(2026, 4, 2, 14, 0));
        request.setEndTime(LocalDateTime.of(2026, 4, 2, 16, 0));
        request.setCategory("알고리즘");
        scheduleService.create(user.getId(), request);

        Long scheduleId = scheduleRepository.findAll().get(0).getId();
        scheduleService.delete(user.getId(), scheduleId);

        assertEquals(0, scheduleRepository.findAll().size());
    }

    @Test
    void 타인_일정_삭제_불가() {
        ScheduleCreateRequest request = new ScheduleCreateRequest();
        request.setTitle("알고리즘 공부");
        request.setStartTime(LocalDateTime.of(2026, 4, 2, 14, 0));
        request.setEndTime(LocalDateTime.of(2026, 4, 2, 16, 0));
        request.setCategory("알고리즘");
        scheduleService.create(user.getId(), request);

        User other = userRepository.save(User.builder()
                .email("other@test.com")
                .password("password1234")
                .nickname("타인")
                .age((byte) 26)
                .build());

        Long scheduleId = scheduleRepository.findAll().get(0).getId();

        assertThrows(IllegalArgumentException.class,
                () -> scheduleService.delete(other.getId(), scheduleId));
    }
}