package com.ohjeon.life_is_egg.domain.schedule.service;

import com.ohjeon.life_is_egg.domain.auth.entity.User;
import com.ohjeon.life_is_egg.domain.auth.repository.UserRepository;
import com.ohjeon.life_is_egg.domain.schedule.dto.ScheduleCreateRequest;
import com.ohjeon.life_is_egg.domain.schedule.dto.ScheduleResponse;
import com.ohjeon.life_is_egg.domain.schedule.entity.Schedule;
import com.ohjeon.life_is_egg.domain.schedule.repository.ScheduleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    public List<ScheduleResponse> getSchedules(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        return scheduleRepository.findByUserOrderByStartTimeAsc(user)
                .stream()
                .map(ScheduleResponse::new)
                .toList();
    }

    @Transactional
    public void create(Long userId, ScheduleCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new IllegalArgumentException("종료 시간은 시작 시간 이후여야 합니다.");
        }

        Schedule schedule = Schedule.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .category(request.getCategory())
                .build();

        scheduleRepository.save(schedule);
    }

    @Transactional
    public void update(Long userId, Long scheduleId, ScheduleCreateRequest request) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일정입니다."));

        if (!schedule.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 일정만 수정할 수 있습니다.");
        }

        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new IllegalArgumentException("종료 시간은 시작 시간 이후여야 합니다.");
        }

        schedule.update(request.getTitle(), request.getDescription(),
                request.getStartTime(), request.getEndTime(), request.getCategory());
    }

    @Transactional
    public void toggleComplete(Long userId, Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일정입니다."));

        if (!schedule.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 일정만 수정할 수 있습니다.");
        }

        schedule.toggleComplete();
    }

    @Transactional
    public void delete(Long userId, Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일정입니다."));

        if (!schedule.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 일정만 삭제할 수 있습니다.");
        }

        scheduleRepository.delete(schedule);
    }
}