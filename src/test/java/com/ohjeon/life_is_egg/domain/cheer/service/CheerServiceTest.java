package com.ohjeon.life_is_egg.domain.cheer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ohjeon.life_is_egg.domain.auth.entity.User;
import com.ohjeon.life_is_egg.domain.auth.repository.UserRepository;
import com.ohjeon.life_is_egg.domain.cheer.dto.CheerCreateRequest;
import com.ohjeon.life_is_egg.domain.cheer.dto.CheerResponse;
import com.ohjeon.life_is_egg.domain.cheer.entity.Cheer;
import com.ohjeon.life_is_egg.domain.cheer.repository.CheerRepository;
import com.ohjeon.life_is_egg.domain.post.entity.Post;
import com.ohjeon.life_is_egg.domain.post.entity.Visibility;
import com.ohjeon.life_is_egg.domain.post.repository.PostRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
class CheerServiceTest {

    @Autowired
    private CheerService cheerService;

    @Autowired
    private CheerRepository cheerRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private User postOwner;
    private User cheerWriter;
    private Post post;

    @BeforeEach
    void setUp() {
        cheerRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

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

    @Test
    void 응원_작성_성공() {
        CheerCreateRequest request = new CheerCreateRequest();
        request.setContent("응원합니다!");

        cheerService.create(cheerWriter.getId(), post.getUuid(), request);

        List<Cheer> cheers = cheerRepository.findAll();
        assertEquals(1, cheers.size());
        assertEquals("응원합니다!", cheers.get(0).getContent());
    }

    @Test
    void 본인_일기_응원_불가() {
        CheerCreateRequest request = new CheerCreateRequest();
        request.setContent("내 일기에 응원");

        assertThrows(IllegalArgumentException.class,
                () -> cheerService.create(postOwner.getId(), post.getUuid(), request));
    }

    @Test
    void 답글_작성_성공() {
        CheerCreateRequest rootRequest = new CheerCreateRequest();
        rootRequest.setContent("루트 응원");
        cheerService.create(cheerWriter.getId(), post.getUuid(), rootRequest);

        Cheer root = cheerRepository.findAll().get(0);

        CheerCreateRequest replyRequest = new CheerCreateRequest();
        replyRequest.setContent("답글입니다");
        replyRequest.setParentId(root.getId());
        cheerService.create(cheerWriter.getId(), post.getUuid(), replyRequest);

        List<Cheer> cheers = cheerRepository.findAll();
        assertEquals(2, cheers.size());
        assertNotNull(cheers.get(1).getParent());
        assertEquals(root.getId(), cheers.get(1).getParent().getId());
    }

    @Test
    void 응원_목록_트리_조회() {
        CheerCreateRequest rootRequest = new CheerCreateRequest();
        rootRequest.setContent("루트 응원");
        cheerService.create(cheerWriter.getId(), post.getUuid(), rootRequest);

        Cheer root = cheerRepository.findAll().get(0);

        CheerCreateRequest replyRequest = new CheerCreateRequest();
        replyRequest.setContent("답글");
        replyRequest.setParentId(root.getId());
        cheerService.create(cheerWriter.getId(), post.getUuid(), replyRequest);

        List<CheerResponse> result = cheerService.getCheers(post.getUuid());

        assertEquals(1, result.size()); // 루트만 1개
        assertEquals(1, result.get(0).getChildren().size()); // 자식 1개
    }

    @Test
    void 응원_삭제_성공() {
        CheerCreateRequest request = new CheerCreateRequest();
        request.setContent("삭제할 응원");
        cheerService.create(cheerWriter.getId(), post.getUuid(), request);

        Long cheerId = cheerRepository.findAll().get(0).getId();
        cheerService.delete(postOwner.getId(), cheerId);

        assertEquals(0, cheerRepository.findAll().size());
    }

    @Test
    void 일기_주인_아닌_사람_응원_삭제_불가() {
        CheerCreateRequest request = new CheerCreateRequest();
        request.setContent("응원");
        cheerService.create(cheerWriter.getId(), post.getUuid(), request);

        Long cheerId = cheerRepository.findAll().get(0).getId();

        assertThrows(IllegalArgumentException.class,
                () -> cheerService.delete(cheerWriter.getId(), cheerId));
    }
}