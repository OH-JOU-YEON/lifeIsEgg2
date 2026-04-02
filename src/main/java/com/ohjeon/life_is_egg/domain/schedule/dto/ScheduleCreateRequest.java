package com.ohjeon.life_is_egg.domain.schedule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ScheduleCreateRequest {

    @NotBlank(message = "일정 제목을 입력해주세요")
    @Size(max = 100)
    private String title;

    @Size(max = 500)
    private String description;

    @NotNull(message = "시작 시간을 입력해주세요")
    private LocalDateTime startTime;

    @NotNull(message = "종료 시간을 입력해주세요")
    private LocalDateTime endTime;

    @NotBlank(message = "카테고리를 선택해주세요")
    private String category;
}