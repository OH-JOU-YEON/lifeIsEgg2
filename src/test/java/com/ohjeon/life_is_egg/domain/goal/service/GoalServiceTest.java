package com.ohjeon.life_is_egg.domain.goal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ohjeon.life_is_egg.domain.auth.entity.User;
import com.ohjeon.life_is_egg.domain.auth.repository.UserRepository;
import com.ohjeon.life_is_egg.domain.goal.dto.GoalCreateRequest;
import com.ohjeon.life_is_egg.domain.goal.repository.GoalRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
class GoalServiceTest {

    @Autowired
    private GoalService goalService;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        goalRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .email("test@test2.com")
                .password("password1234")
                .nickname("테스터2")
                .age((byte) 26)
                .build());
    }

    @Test
    void 목표_등록_성공() {
        GoalCreateRequest request = new GoalCreateRequest();
        request.setTitle("이번 주 5문제 풀기");
        request.setTargetValue(5);
        request.setUnit("문제");
        request.setStartDate(LocalDate.of(2026, 4, 2));
        request.setEndDate(LocalDate.of(2026, 4, 9));

        goalService.create(user.getId(), request);

        assertEquals(1, goalRepository.findAll().size());
    }

    @Test
    void 종료일이_시작일보다_이전이면_실패() {
        GoalCreateRequest request = new GoalCreateRequest();
        request.setTitle("이번 주 5문제 풀기");
        request.setTargetValue(5);
        request.setUnit("문제");
        request.setStartDate(LocalDate.of(2026, 4, 9));
        request.setEndDate(LocalDate.of(2026, 4, 2));

        assertThrows(IllegalArgumentException.class,
                () -> goalService.create(user.getId(), request));
    }

    @Test
    void 진행도_증가() {
        GoalCreateRequest request = new GoalCreateRequest();
        request.setTitle("이번 주 5문제 풀기");
        request.setTargetValue(5);
        request.setUnit("문제");
        request.setStartDate(LocalDate.of(2026, 4, 2));
        request.setEndDate(LocalDate.of(2026, 4, 9));
        goalService.create(user.getId(), request);

        Long goalId = goalRepository.findAll().get(0).getId();
        goalService.updateProgress(user.getId(), goalId, 1);

        assertEquals(1, goalRepository.findAll().get(0).getCurrentValue());
    }

    @Test
    void 진행도_0_미만_안됨() {
        GoalCreateRequest request = new GoalCreateRequest();
        request.setTitle("이번 주 5문제 풀기");
        request.setTargetValue(5);
        request.setUnit("문제");
        request.setStartDate(LocalDate.of(2026, 4, 2));
        request.setEndDate(LocalDate.of(2026, 4, 9));
        goalService.create(user.getId(), request);

        Long goalId = goalRepository.findAll().get(0).getId();
        goalService.updateProgress(user.getId(), goalId, -1);

        assertEquals(0, goalRepository.findAll().get(0).getCurrentValue());
    }

    @Test
    void 목표_달성_시_완료_처리() {
        GoalCreateRequest request = new GoalCreateRequest();
        request.setTitle("이번 주 2문제 풀기");
        request.setTargetValue(2);
        request.setUnit("문제");
        request.setStartDate(LocalDate.of(2026, 4, 2));
        request.setEndDate(LocalDate.of(2026, 4, 9));
        goalService.create(user.getId(), request);

        Long goalId = goalRepository.findAll().get(0).getId();
        goalService.updateProgress(user.getId(), goalId, 1);
        goalService.updateProgress(user.getId(), goalId, 1);

        assertTrue(goalRepository.findAll().get(0).isCompleted());
    }

    @Test
    void 목표_수정_성공() {
        GoalCreateRequest request = new GoalCreateRequest();
        request.setTitle("이번 주 5문제 풀기");
        request.setTargetValue(5);
        request.setUnit("문제");
        request.setStartDate(LocalDate.of(2026, 4, 2));
        request.setEndDate(LocalDate.of(2026, 4, 9));
        goalService.create(user.getId(), request);

        Long goalId = goalRepository.findAll().get(0).getId();

        GoalCreateRequest updateRequest = new GoalCreateRequest();
        updateRequest.setTitle("이번 주 7문제 풀기");
        updateRequest.setTargetValue(7);
        updateRequest.setUnit("문제");
        updateRequest.setStartDate(LocalDate.of(2026, 4, 2));
        updateRequest.setEndDate(LocalDate.of(2026, 4, 9));
        goalService.update(user.getId(), goalId, updateRequest);

        assertEquals("이번 주 7문제 풀기", goalRepository.findAll().get(0).getTitle());
        assertEquals(7, goalRepository.findAll().get(0).getTargetValue());
    }

    @Test
    void 목표_삭제_성공() {
        GoalCreateRequest request = new GoalCreateRequest();
        request.setTitle("이번 주 5문제 풀기");
        request.setTargetValue(5);
        request.setUnit("문제");
        request.setStartDate(LocalDate.of(2026, 4, 2));
        request.setEndDate(LocalDate.of(2026, 4, 9));
        goalService.create(user.getId(), request);

        Long goalId = goalRepository.findAll().get(0).getId();
        goalService.delete(user.getId(), goalId);

        assertEquals(0, goalRepository.findAll().size());
    }

    @Test
    void 타인_목표_삭제_불가() {
        GoalCreateRequest request = new GoalCreateRequest();
        request.setTitle("이번 주 5문제 풀기");
        request.setTargetValue(5);
        request.setUnit("문제");
        request.setStartDate(LocalDate.of(2026, 4, 2));
        request.setEndDate(LocalDate.of(2026, 4, 9));
        goalService.create(user.getId(), request);

        User other = userRepository.save(User.builder()
                .email("other@test.com")
                .password("password1234")
                .nickname("타인")
                .age((byte) 26)
                .build());

        Long goalId = goalRepository.findAll().get(0).getId();

        assertThrows(IllegalArgumentException.class,
                () -> goalService.delete(other.getId(), goalId));
    }
}