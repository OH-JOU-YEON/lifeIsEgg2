package com.ohjeon.life_is_egg.domain.alarm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ohjeon.life_is_egg.domain.alarm.dto.AlarmResponse;
import com.ohjeon.life_is_egg.domain.alarm.entity.Alarm;
import com.ohjeon.life_is_egg.domain.alarm.repository.AlarmRepository;
import com.ohjeon.life_is_egg.domain.auth.entity.User;
import com.ohjeon.life_is_egg.domain.auth.repository.UserRepository;
import com.ohjeon.life_is_egg.domain.cheer.dto.CheerCreateRequest;
import com.ohjeon.life_is_egg.domain.cheer.repository.CheerRepository;
import com.ohjeon.life_is_egg.domain.cheer.service.CheerService;
import com.ohjeon.life_is_egg.domain.goal.repository.GoalRepository;
import com.ohjeon.life_is_egg.domain.post.entity.Post;
import com.ohjeon.life_is_egg.domain.post.entity.Visibility;
import com.ohjeon.life_is_egg.domain.post.repository.PostRepository;
import com.ohjeon.life_is_egg.domain.schedule.repository.ScheduleRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.transaction.TestTransaction;

@SpringBootTest
@Transactional
class AlarmServiceTest {

    @Autowired
    private AlarmService alarmService;

    @Autowired
    private CheerService cheerService;

    @Autowired
    private AlarmRepository alarmRepository;

    @Autowired
    private CheerRepository cheerRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    private User postOwner;
    private User cheerWriter;
    private Post post;

    @BeforeEach
    void setUp() {
        alarmRepository.deleteAllInBatch();
        cheerRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
        goalRepository.deleteAllInBatch();
        scheduleRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        postOwner = userRepository.save(User.builder()
                .email("owner@test.com")
                .password("password1234")
                .nickname("일기주인")
                .age((byte) 26)
                .build());

        cheerWriter = userRepository.save(User.builder()
                .email("writer@test.com")
                .password("password1234")
                .nickname("응원작성자")
                .age((byte) 26)
                .build());

        post = postRepository.save(Post.builder()
                .user(postOwner)
                .content("테스트 일기")
                .visibility(Visibility.PUBLIC)
                .build());
    }

    // cheerService.create() 이후 AFTER_COMMIT 리스너를 강제로 실행시키기 위한 헬퍼
    private void commitAndStartNewTransaction() {
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();
    }

    @Test
    void 응원_작성_시_알림_생성() {
        CheerCreateRequest request = new CheerCreateRequest();
        request.setContent("응원합니다!");

        cheerService.create(cheerWriter.getId(), post.getUuid(), request);
        commitAndStartNewTransaction();

        List<Alarm> alarms = alarmRepository.findAll();
        assertEquals(1, alarms.size());
        assertEquals(postOwner.getId(), alarms.get(0).getUser().getId());
    }

    @Test
    void 읽지_않은_알림_개수_조회() {
        CheerCreateRequest request = new CheerCreateRequest();
        request.setContent("응원1");
        cheerService.create(cheerWriter.getId(), post.getUuid(), request);

        request = new CheerCreateRequest();
        request.setContent("응원2");
        cheerService.create(cheerWriter.getId(), post.getUuid(), request);
        commitAndStartNewTransaction();

        long count = alarmService.getUnreadCount(postOwner.getId());
        assertEquals(2, count);
    }

    @Test
    void 알림_읽음_처리() {
        CheerCreateRequest request = new CheerCreateRequest();
        request.setContent("응원합니다!");
        cheerService.create(cheerWriter.getId(), post.getUuid(), request);
        commitAndStartNewTransaction();

        Long alarmId = alarmRepository.findAll().get(0).getId();
        alarmService.readAlarm(postOwner.getId(), alarmId);

        Alarm alarm = alarmRepository.findById(alarmId).get();
        assertTrue(alarm.isRead());
    }

    @Test
    void 타인_알림_읽음_처리_불가() {
        CheerCreateRequest request = new CheerCreateRequest();
        request.setContent("응원합니다!");
        cheerService.create(cheerWriter.getId(), post.getUuid(), request);
        commitAndStartNewTransaction();

        Long alarmId = alarmRepository.findAll().get(0).getId();

        assertThrows(IllegalArgumentException.class,
                () -> alarmService.readAlarm(cheerWriter.getId(), alarmId));
    }

    @Test
    void 알림_목록_조회() {
        CheerCreateRequest request = new CheerCreateRequest();
        request.setContent("응원1");
        cheerService.create(cheerWriter.getId(), post.getUuid(), request);

        request = new CheerCreateRequest();
        request.setContent("응원2");
        cheerService.create(cheerWriter.getId(), post.getUuid(), request);
        commitAndStartNewTransaction();

        List<AlarmResponse> alarms = alarmService.getAlarms(postOwner.getId());
        assertEquals(2, alarms.size());
    }
}