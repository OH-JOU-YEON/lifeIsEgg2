package com.ohjeon.life_is_egg.domain.post.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ohjeon.life_is_egg.domain.auth.entity.User;
import com.ohjeon.life_is_egg.domain.auth.repository.UserRepository;
import com.ohjeon.life_is_egg.domain.post.dto.PostCreateRequest;
import com.ohjeon.life_is_egg.domain.post.dto.PostDetailResponse;
import com.ohjeon.life_is_egg.domain.post.dto.PostMyResponse;
import com.ohjeon.life_is_egg.domain.post.entity.Post;
import com.ohjeon.life_is_egg.domain.post.entity.Visibility;
import com.ohjeon.life_is_egg.domain.post.repository.PostRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@SpringBootTest
@Transactional
class PostServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll(); // 추가
        testUser = userRepository.save(User.builder()
                .email("test@test2.com")
                .password("password1234")
                .nickname("테스터2")
                .age((byte) 26)
                .build());
    }

    @Test
    void 일기_작성_성공() {
        PostCreateRequest request = new PostCreateRequest();
        request.setTitle("테스트 제목");
        request.setContent("테스트 내용입니다.");
        request.setVisibility(Visibility.PUBLIC);

        postService.create(testUser.getId(), request);

        List<Post> posts = postRepository.findAll();
        assertEquals(1, posts.size());
        assertEquals("테스트 내용입니다.", posts.get(0).getContent());
    }

    @Test
    void 일기_작성_시_uuid_자동생성() {
        PostCreateRequest request = new PostCreateRequest();
        request.setContent("내용");
        request.setVisibility(Visibility.PUBLIC);

        postService.create(testUser.getId(), request);

        Post post = postRepository.findAll().get(0);
        assertNotNull(post.getUuid());
    }

    @Test
    void 내_일기_목록_조회() {
        PostCreateRequest request = new PostCreateRequest();
        request.setContent("내용");
        request.setVisibility(Visibility.PUBLIC);
        postService.create(testUser.getId(), request);

        Page<PostMyResponse> result = postService.getMyPosts(
                testUser.getId(), PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void 일기_상세_조회_성공() {
        PostCreateRequest request = new PostCreateRequest();
        request.setContent("상세 조회 테스트");
        request.setVisibility(Visibility.PUBLIC);
        postService.create(testUser.getId(), request);

        String uuid = postRepository.findAll().get(0).getUuid();
        PostDetailResponse response = postService.getPost(testUser.getId(), uuid);

        assertEquals("상세 조회 테스트", response.getContent());
        assertTrue(response.isOwner());
    }

    @Test
    void 비공개_일기_타인_조회_실패() {
        PostCreateRequest request = new PostCreateRequest();
        request.setContent("비공개 일기");
        request.setVisibility(Visibility.PRIVATE);
        postService.create(testUser.getId(), request);

        User other = userRepository.save(User.builder()
                .email("other@test.com")
                .password("password1234")
                .nickname("타인")
                .age((byte) 26)
                .build());

        String uuid = postRepository.findAll().get(0).getUuid();

        assertThrows(IllegalArgumentException.class,
                () -> postService.getPost(other.getId(), uuid));
    }

    @Test
    void 일기_수정_성공() {
        PostCreateRequest request = new PostCreateRequest();
        request.setContent("원래 내용");
        request.setVisibility(Visibility.PUBLIC);
        postService.create(testUser.getId(), request);

        String uuid = postRepository.findAll().get(0).getUuid();

        PostCreateRequest updateRequest = new PostCreateRequest();
        updateRequest.setContent("수정된 내용");
        updateRequest.setVisibility(Visibility.PRIVATE);
        postService.update(testUser.getId(), uuid, updateRequest);

        Post updated = postRepository.findAll().get(0);
        assertEquals("수정된 내용", updated.getContent());
    }

    @Test
    void 타인_일기_수정_실패() {
        PostCreateRequest request = new PostCreateRequest();
        request.setContent("내용");
        request.setVisibility(Visibility.PUBLIC);
        postService.create(testUser.getId(), request);

        User other = userRepository.save(User.builder()
                .email("other@test.com")
                .password("password1234")
                .nickname("타인")
                .age((byte) 26)
                .build());

        String uuid = postRepository.findAll().get(0).getUuid();

        PostCreateRequest updateRequest = new PostCreateRequest();
        updateRequest.setContent("수정 시도");
        updateRequest.setVisibility(Visibility.PUBLIC);

        assertThrows(IllegalArgumentException.class,
                () -> postService.update(other.getId(), uuid, updateRequest));
    }

    @Test
    void 일기_삭제_성공() {
        PostCreateRequest request = new PostCreateRequest();
        request.setContent("삭제할 내용");
        request.setVisibility(Visibility.PUBLIC);
        postService.create(testUser.getId(), request);

        String uuid = postRepository.findAll().get(0).getUuid();
        postService.delete(testUser.getId(), uuid);

        Post deleted = postRepository.findAll().get(0);
        assertTrue(deleted.isDeleted());
    }

    @Test
    void 타인_일기_삭제_실패() {
        PostCreateRequest request = new PostCreateRequest();
        request.setContent("내용");
        request.setVisibility(Visibility.PUBLIC);
        postService.create(testUser.getId(), request);

        User other = userRepository.save(User.builder()
                .email("other@test.com")
                .password("password1234")
                .nickname("타인")
                .age((byte) 26)
                .build());

        String uuid = postRepository.findAll().get(0).getUuid();

        assertThrows(IllegalArgumentException.class,
                () -> postService.delete(other.getId(), uuid));
    }
}