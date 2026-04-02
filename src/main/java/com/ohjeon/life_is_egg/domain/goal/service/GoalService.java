package com.ohjeon.life_is_egg.domain.goal.service;

import com.ohjeon.life_is_egg.domain.auth.entity.User;
import com.ohjeon.life_is_egg.domain.auth.repository.UserRepository;
import com.ohjeon.life_is_egg.domain.goal.dto.GoalCreateRequest;
import com.ohjeon.life_is_egg.domain.goal.dto.GoalResponse;
import com.ohjeon.life_is_egg.domain.goal.entity.Goal;
import com.ohjeon.life_is_egg.domain.goal.repository.GoalRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;

    public List<GoalResponse> getGoals(Long userId, boolean completed) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        return goalRepository.findByUserAndCompletedOrderByEndDateAsc(user, completed)
                .stream()
                .map(GoalResponse::new)
                .toList();
    }

    public void create(Long userId, GoalCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("종료일은 시작일 이후여야 합니다.");
        }

        Goal goal = Goal.builder()
                .user(user)
                .title(request.getTitle())
                .targetValue(request.getTargetValue())
                .unit(request.getUnit())
                .category(request.getCategory())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        goalRepository.save(goal);
    }

    public GoalResponse updateProgress(Long userId, Long goalId, int increment) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 목표입니다."));

        if (!goal.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 목표만 수정할 수 있습니다.");
        }

        if (increment != 1 && increment != -1) {
            throw new IllegalArgumentException("increment는 1 또는 -1만 가능합니다.");
        }

        goal.updateProgress(increment);
        return new GoalResponse(goal);
    }

    public void update(Long userId, Long goalId, GoalCreateRequest request) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 목표입니다."));

        if (!goal.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 목표만 수정할 수 있습니다.");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("종료일은 시작일 이후여야 합니다.");
        }

        goal.update(request.getTitle(), request.getTargetValue(),
                request.getEndDate(), request.getCategory());
    }

    public void delete(Long userId, Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 목표입니다."));

        if (!goal.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 목표만 삭제할 수 있습니다.");
        }

        goalRepository.delete(goal);
    }
}