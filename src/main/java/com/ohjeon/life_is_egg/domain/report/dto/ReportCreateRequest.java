package com.ohjeon.life_is_egg.domain.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReportCreateRequest {

    private Long postId;

    private Long cheerId;

    @NotBlank(message = "신고 사유를 입력해주세요")
    @Size(max = 500)
    private String reason;
}