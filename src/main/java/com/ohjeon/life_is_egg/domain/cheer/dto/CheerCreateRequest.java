package com.ohjeon.life_is_egg.domain.cheer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CheerCreateRequest {

    @NotBlank(message = "응원 내용을 입력해주세요")
    @Size(max = 500, message = "최대 500자까지 작성 가능합니다")
    private String content;

    private Long parentId; // 답글이면 부모 응원 id, 루트면 null
}