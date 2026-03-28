package com.ohjeon.life_is_egg.domain.post.dto;

import com.ohjeon.life_is_egg.domain.post.entity.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostCreateRequest {

    @NotBlank(message = "일기 내용을 입력해주세요")
    @Size(max = 5000, message = "최대 5000자까지 작성 가능합니다")
    private String content;

    @Size(max = 100)
    private String title;

    @NotNull
    private Visibility visibility;
}