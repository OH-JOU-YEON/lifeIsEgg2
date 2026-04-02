package com.ohjeon.life_is_egg.domain.goal.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GoalCreateRequest {

    @NotBlank(message = "목표 제목을 입력해주세요")
    @Size(max = 100)
    private String title;

    @NotNull(message = "목표 수치를 입력해주세요")
    @Min(value = 1, message = "목표 수치는 1 이상이어야 합니다")
    private Integer targetValue;

    @NotBlank(message = "단위를 입력해주세요")
    @Size(max = 20)
    private String unit;

    private String category;

    @NotNull(message = "시작일을 입력해주세요")
    private LocalDate startDate;

    @NotNull(message = "종료일을 입력해주세요")
    private LocalDate endDate;
}