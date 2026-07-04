package com.ohjeon.life_is_egg.domain.post.service;

import com.ohjeon.life_is_egg.domain.auth.entity.User;
import com.ohjeon.life_is_egg.domain.auth.repository.UserRepository;
import com.ohjeon.life_is_egg.domain.post.dto.PostCreateRequest;
import com.ohjeon.life_is_egg.domain.post.dto.PostDetailResponse;
import com.ohjeon.life_is_egg.domain.post.dto.PostFeedResponse;
import com.ohjeon.life_is_egg.domain.post.dto.PostMyResponse;
import com.ohjeon.life_is_egg.domain.post.entity.Post;
import com.ohjeon.life_is_egg.domain.post.entity.Visibility;
import com.ohjeon.life_is_egg.domain.post.port.CheerCountPort;
import com.ohjeon.life_is_egg.domain.post.repository.PostRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CheerCountPort cheerCountPort;

    @Transactional
    public void create(Long userId, PostCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

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

    public Page<PostMyResponse> getMyPosts(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        return postRepository.findByUserAndDeletedFalseOrderByCreatedAtDesc(user, pageable)
                .map(PostMyResponse::new);
    }

    public List<PostFeedResponse> getFeed(Long userId, List<Long> excludeIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        List<Post> posts;
        if (user.getAge() == null) {
            posts = postRepository.findFeedPosts(excludeIds, excludeIds.isEmpty(), (byte) 0, (byte) 127);
        } else {
            Byte age = user.getAge();
            Byte minAge = (byte) Math.max(0, age - 5);
            Byte maxAge = (byte) (age + 5);
            posts = postRepository.findFeedPosts(excludeIds, excludeIds.isEmpty(), minAge, maxAge);

            if (posts.isEmpty()) {
                posts = postRepository.findFeedPosts(excludeIds, excludeIds.isEmpty(), (byte) 0, (byte) 127);
            }
        }

        if (posts.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = posts.stream().map(Post::getId).toList();
        Map<Long, Long> cheerCountMap = cheerCountPort.countByPostIds(postIds);

        return posts.stream()
                .map(post -> new PostFeedResponse(post, cheerCountMap.getOrDefault(post.getId(), 0L)))
                .toList();
    }

    public PostDetailResponse getPost(Long userId, String uuid) {
        Post post = postRepository.findByUuidAndDeletedFalse(uuid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일기입니다."));

        if (post.getVisibility() == Visibility.PRIVATE && !post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("비공개 일기입니다.");
        }

        boolean isOwner = post.getUser().getId().equals(userId);
        long cheerCount = cheerCountPort.countByPost(post);
        return new PostDetailResponse(post, isOwner, cheerCount);
    }

    @Transactional
    public void update(Long userId, String uuid, PostCreateRequest request) {
        Post post = postRepository.findByUuidAndDeletedFalse(uuid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일기입니다."));

        if (!post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 일기만 수정할 수 있습니다.");
        }

        post.update(request.getTitle(), request.getContent(), request.getVisibility());
    }

    @Transactional
    public void delete(Long userId, String uuid) {
        Post post = postRepository.findByUuidAndDeletedFalse(uuid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일기입니다."));

        if (!post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 일기만 삭제할 수 있습니다.");
        }

        post.delete();
    }
}