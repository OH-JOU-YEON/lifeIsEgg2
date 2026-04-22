package com.ohjeon.life_is_egg.domain.dashboard.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.ohjeon.life_is_egg.domain.auth.entity.User;
import com.ohjeon.life_is_egg.domain.auth.repository.UserRepository;
import com.ohjeon.life_is_egg.domain.cheer.entity.Cheer;
import com.ohjeon.life_is_egg.domain.cheer.repository.CheerRepository;
import com.ohjeon.life_is_egg.domain.dashboard.dto.DashboardStatsResponse;
import com.ohjeon.life_is_egg.domain.goal.entity.Goal;
import com.ohjeon.life_is_egg.domain.goal.repository.GoalRepository;
import com.ohjeon.life_is_egg.domain.post.entity.Post;
import com.ohjeon.life_is_egg.domain.post.entity.Visibility;
import com.ohjeon.life_is_egg.domain.post.repository.PostRepository;
import com.ohjeon.life_is_egg.domain.schedule.entity.Schedule;
import com.ohjeon.life_is_egg.domain.schedule.repository.ScheduleRepository;
import jakarta.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
class DashboardServiceTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CheerRepository cheerRepository;

    private User user;

    @BeforeEach
    void setUp() {
        cheerRepository.deleteAll();
        postRepository.deleteAll();
        scheduleRepository.deleteAll();
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
    void 이번주_목표_달성률_계산() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);

        goalRepository.save(Goal.builder()
                .user(user)
                .title("목표1")
                .targetValue(5)
                .unit("문제")
                .startDate(weekStart)
                .endDate(weekStart.plusDays(6))
                .build());

        Goal completedGoal = goalRepository.save(Goal.builder()
                .user(user)
                .title("목표2")
                .targetValue(1)
                .unit("문제")
                .startDate(weekStart)
                .endDate(weekStart.plusDays(6))
                .build());

        completedGoal.updateProgress(1);

        DashboardStatsResponse stats = dashboardService.getStats(user.getId());

        assertEquals(2, stats.getWeeklyGoal().totalCount());
        assertEquals(1, stats.getWeeklyGoal().completedCount());
        assertEquals(50.0, stats.getWeeklyGoal().achievementRate());
    }

    @Test
    void 목표_없으면_달성률_0() {
        DashboardStatsResponse stats = dashboardService.getStats(user.getId());
        assertEquals(0.0, stats.getWeeklyGoal().achievementRate());
    }

    @Test
    void 카테고리별_학습시간_집계() {
        scheduleRepository.save(Schedule.builder()
                .user(user)
                .title("알고리즘 공부")
                .startTime(LocalDateTime.now().minusHours(3))
                .endTime(LocalDateTime.now().minusHours(1))
                .category("알고리즘")
                .build());

        Schedule schedule = scheduleRepository.findAll().get(0);
        schedule.toggleComplete();

        DashboardStatsResponse stats = dashboardService.getStats(user.getId());

        assertFalse(stats.getCategoryTime().isEmpty());
        assertEquals("알고리즘", stats.getCategoryTime().get(0).category());
    }

    @Test
    void 이번달_활동요약() {
        User other = userRepository.save(User.builder()
                .email("other@test.com")
                .password("password1234")
                .nickname("타인")
                .age((byte) 26)
                .build());

        Post post = postRepository.save(Post.builder()
                .user(user)
                .content("테스트 일기")
                .visibility(Visibility.PUBLIC)
                .build());

        cheerRepository.save(Cheer.builder()
                .user(other)
                .post(post)
                .content("응원합니다")
                .build());

        DashboardStatsResponse stats = dashboardService.getStats(user.getId());

        assertEquals(1, stats.getActivitySummary().diaryCount());
        assertEquals(1, stats.getActivitySummary().cheerCount());
    }
}