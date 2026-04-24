package com.ohjeon.life_is_egg.domain.cheer.service;

import com.ohjeon.life_is_egg.domain.alarm.service.AlarmService;
import com.ohjeon.life_is_egg.domain.auth.entity.User;
import com.ohjeon.life_is_egg.domain.auth.repository.UserRepository;
import com.ohjeon.life_is_egg.domain.cheer.dto.CheerCreateRequest;
import com.ohjeon.life_is_egg.domain.cheer.dto.CheerResponse;
import com.ohjeon.life_is_egg.domain.cheer.entity.Cheer;
import com.ohjeon.life_is_egg.domain.cheer.repository.CheerRepository;
import com.ohjeon.life_is_egg.domain.post.entity.Post;
import com.ohjeon.life_is_egg.domain.post.repository.PostRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheerService {

    private final CheerRepository cheerRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final AlarmService alarmService;

    // 응원 목록 조회 (트리 구조)
    public List<CheerResponse> getCheers(String postUuid) {
        Post post = postRepository.findByUuidAndDeletedFalse(postUuid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일기입니다."));

        List<Cheer> cheers = cheerRepository.findByPostOrderByCreatedAtAsc(post);

        // flat 리스트 → 트리 조립
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

    // 응원 작성
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

        // 일기 주인에게 알람 (본인이 작성한 경우 제외)
        if (!post.getUser().getId().equals(userId)) {
            alarmService.createCheerAlarm(post.getUser(), post, cheer);
        }

        // 답글이면 부모 응원 작성자에게도 알람 (일기 주인이거나 본인이면 제외)
        if (parent != null
                && !parent.getUser().getId().equals(post.getUser().getId())
                && !parent.getUser().getId().equals(userId)) {
            alarmService.createCheerAlarm(parent.getUser(), post, cheer);
        }
    }

    // 응원 삭제
    public void delete(Long userId, Long cheerId) {
        Cheer cheer = cheerRepository.findById(cheerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 응원입니다."));

        if (!cheer.getPost().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("일기 주인만 응원을 삭제할 수 있습니다.");
        }

        cheerRepository.delete(cheer);
    }
}