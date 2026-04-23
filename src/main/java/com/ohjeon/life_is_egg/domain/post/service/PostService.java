package com.ohjeon.life_is_egg.domain.post.service;

import com.ohjeon.life_is_egg.domain.auth.entity.User;
import com.ohjeon.life_is_egg.domain.auth.repository.UserRepository;
import com.ohjeon.life_is_egg.domain.post.dto.PostCreateRequest;
import com.ohjeon.life_is_egg.domain.post.dto.PostDetailResponse;
import com.ohjeon.life_is_egg.domain.post.dto.PostFeedResponse;
import com.ohjeon.life_is_egg.domain.post.dto.PostMyResponse;
import com.ohjeon.life_is_egg.domain.post.entity.Post;
import com.ohjeon.life_is_egg.domain.post.entity.Visibility;
import com.ohjeon.life_is_egg.domain.post.repository.PostRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 일기 작성
    public void create(Long userId, PostCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        // 하루 1개 제한
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        long count = postRepository.countByUserAndCreatedAtBetweenAndDeletedFalse(user, startOfDay, endOfDay);
        if (count > 0) {
            throw new IllegalArgumentException("하루에 일기는 1개만 작성할 수 있습니다.");
        }

        Post post = Post.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .visibility(request.getVisibility())
                .build();

        postRepository.save(post);
    }

    // 내 일기 목록
    public Page<PostMyResponse> getMyPosts(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        return postRepository.findByUserAndDeletedFalseOrderByCreatedAtDesc(user, pageable)
                .map(PostMyResponse::new);
    }

    // 또래 피드
    public List<PostFeedResponse> getFeed(Long userId, List<Long> excludeIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Byte age = user.getAge();
        Byte minAge = (byte) Math.max(0, age - 5);
        Byte maxAge = (byte) (age + 5);

        return postRepository.findFeedPosts(excludeIds, excludeIds.isEmpty(), minAge, maxAge)
                .stream()
                .map(PostFeedResponse::new)
                .toList();
    }

    // 일기 상세
    public PostDetailResponse getPost(Long userId, String uuid) {
        Post post = postRepository.findByUuidAndDeletedFalse(uuid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일기입니다."));

        if (post.getVisibility() == Visibility.PRIVATE && !post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("비공개 일기입니다.");
        }

        boolean isOwner = post.getUser().getId().equals(userId);
        return new PostDetailResponse(post, isOwner);
    }

    // 일기 수정
    public void update(Long userId, String uuid, PostCreateRequest request) {
        Post post = postRepository.findByUuidAndDeletedFalse(uuid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일기입니다."));

        if (!post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 일기만 수정할 수 있습니다.");
        }

        post.update(request.getTitle(), request.getContent(), request.getVisibility());
    }

    // 일기 삭제
    public void delete(Long userId, String uuid) {
        Post post = postRepository.findByUuidAndDeletedFalse(uuid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일기입니다."));

        if (!post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 일기만 삭제할 수 있습니다.");
        }

        post.delete();
    }
}