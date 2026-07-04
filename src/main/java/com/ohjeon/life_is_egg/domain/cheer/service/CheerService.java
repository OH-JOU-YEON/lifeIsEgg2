package com.ohjeon.life_is_egg.domain.cheer.service;

import com.ohjeon.life_is_egg.domain.auth.entity.User;
import com.ohjeon.life_is_egg.domain.auth.repository.UserRepository;
import com.ohjeon.life_is_egg.domain.cheer.dto.CheerCreateRequest;
import com.ohjeon.life_is_egg.domain.cheer.dto.CheerResponse;
import com.ohjeon.life_is_egg.domain.cheer.entity.Cheer;
import com.ohjeon.life_is_egg.domain.cheer.event.CheerAlarmTarget;
import com.ohjeon.life_is_egg.domain.cheer.event.CheerAlarmType;
import com.ohjeon.life_is_egg.domain.cheer.event.CheerCreatedEvent;
import com.ohjeon.life_is_egg.domain.cheer.event.CheerDeletedEvent;
import com.ohjeon.life_is_egg.domain.cheer.repository.CheerRepository;
import com.ohjeon.life_is_egg.domain.post.entity.Post;
import com.ohjeon.life_is_egg.domain.post.port.CheerCountPort;
import com.ohjeon.life_is_egg.domain.post.repository.PostRepository;
import com.ohjeon.life_is_egg.domain.report.port.CheerLookupPort;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheerService implements CheerCountPort, CheerLookupPort {

    private final CheerRepository cheerRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public List<CheerResponse> getCheers(String postUuid) {
        Post post = postRepository.findByUuidAndDeletedFalse(postUuid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일기입니다."));

        List<Cheer> cheers = cheerRepository.findByPostOrderByCreatedAtAsc(post);

        Map<Long, CheerResponse> map = new LinkedHashMap<>();
        List<CheerResponse> roots = new ArrayList<>();

        for (Cheer cheer : cheers) {
            CheerResponse response = new CheerResponse(cheer);
            map.put(cheer.getId(), response);

            if (cheer.getParent() == null) {
                roots.add(response);
            } else {
                CheerResponse parent = map.get(cheer.getParent().getId());
                if (parent != null) {
                    parent.addChild(response);
                }
            }
        }

        return roots;
    }

    @Transactional
    public void create(Long userId, String postUuid, CheerCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Post post = postRepository.findByUuidAndDeletedFalse(postUuid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일기입니다."));

        if (post.getUser().getId().equals(userId) && request.getParentId() == null) {
            throw new IllegalArgumentException("본인 일기에는 응원할 수 없습니다.");
        }

        Cheer parent = null;
        if (request.getParentId() != null) {
            parent = cheerRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 응원입니다."));
        }

        Cheer cheer = Cheer.builder()
                .user(user)
                .post(post)
                .parent(parent)
                .content(request.getContent())
                .build();

        cheerRepository.save(cheer);

        List<CheerAlarmTarget> targets = new ArrayList<>();

        if (!post.getUser().getId().equals(userId)) {
            targets.add(new CheerAlarmTarget(post.getUser().getId(), CheerAlarmType.POST_CHEER));
        }

        if (parent != null
                && !parent.getUser().getId().equals(post.getUser().getId())
                && !parent.getUser().getId().equals(userId)) {
            targets.add(new CheerAlarmTarget(parent.getUser().getId(), CheerAlarmType.REPLY_CHEER));
        }

        if (!targets.isEmpty()) {
            eventPublisher.publishEvent(
                    new CheerCreatedEvent(targets, post.getId(), post.getUuid(), cheer.getId()));
        }
    }

    @Transactional
    public void delete(Long userId, Long cheerId) {
        Cheer cheer = cheerRepository.findById(cheerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 응원입니다."));

        if (!cheer.getPost().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("일기 주인만 응원을 삭제할 수 있습니다.");
        }

        cheer.delete();

        eventPublisher.publishEvent(new CheerDeletedEvent(cheer.getId()));
    }

    @Override
    public Map<Long, Long> countByPostIds(List<Long> postIds) {
        return cheerRepository.countByPostIds(postIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }

    @Override
    public long countByPost(Post post) {
        return cheerRepository.countByPostAndDeletedFalse(post);
    }

    @Override
    public Cheer getById(Long cheerId) {
        return cheerRepository.findById(cheerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 응원입니다."));
    }
}