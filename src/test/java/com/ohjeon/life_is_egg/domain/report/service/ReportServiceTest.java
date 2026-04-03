package com.ohjeon.life_is_egg.domain.report.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ohjeon.life_is_egg.domain.auth.entity.User;
import com.ohjeon.life_is_egg.domain.auth.repository.UserRepository;
import com.ohjeon.life_is_egg.domain.cheer.entity.Cheer;
import com.ohjeon.life_is_egg.domain.cheer.repository.CheerRepository;
import com.ohjeon.life_is_egg.domain.post.entity.Post;
import com.ohjeon.life_is_egg.domain.post.entity.Visibility;
import com.ohjeon.life_is_egg.domain.post.repository.PostRepository;
import com.ohjeon.life_is_egg.domain.report.dto.ReportCreateRequest;
import com.ohjeon.life_is_egg.domain.report.repository.ReportRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
class ReportServiceTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CheerRepository cheerRepository;

    @Autowired
    private UserRepository userRepository;

    private User reporter;
    private User postOwner;
    private Post post;
    private Cheer cheer;

    @BeforeEach
    void setUp() {
        reportRepository.deleteAll();
        cheerRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        reporter = userRepository.save(User.builder()
                .email("reporter@test.com")
                .password("password1234")
                .nickname("신고자")
                .age((byte) 26)
                .build());

        postOwner = userRepository.save(User.builder()
                .email("owner@test.com")
                .password("password1234")
                .nickname("일기주인")
                .age((byte) 26)
                .build());

        post = postRepository.save(Post.builder()
                .user(postOwner)
                .content("테스트 일기")
                .visibility(Visibility.PUBLIC)
                .build());

        cheer = cheerRepository.save(Cheer.builder()
                .user(reporter)
                .post(post)
                .content("응원합니다")
                .build());
    }

    @Test
    void 일기_신고_성공() {
        ReportCreateRequest request = new ReportCreateRequest();
        request.setPostId(post.getId());
        request.setReason("욕설이 포함되어 있습니다");

        reportService.create(reporter.getId(), request);

        assertEquals(1, reportRepository.findAll().size());
        assertEquals("PENDING", reportRepository.findAll().get(0).getStatus());
    }

    @Test
    void 응원_신고_성공() {
        ReportCreateRequest request = new ReportCreateRequest();
        request.setCheerId(cheer.getId());
        request.setReason("부적절한 내용입니다");

        reportService.create(reporter.getId(), request);

        assertEquals(1, reportRepository.findAll().size());
    }

    @Test
    void 신고_대상_없으면_실패() {
        ReportCreateRequest request = new ReportCreateRequest();
        request.setReason("신고 사유");

        assertThrows(IllegalArgumentException.class,
                () -> reportService.create(reporter.getId(), request));
    }

    @Test
    void 신고_대상_둘_다_있으면_실패() {
        ReportCreateRequest request = new ReportCreateRequest();
        request.setPostId(post.getId());
        request.setCheerId(cheer.getId());
        request.setReason("신고 사유");

        assertThrows(IllegalArgumentException.class,
                () -> reportService.create(reporter.getId(), request));
    }
}