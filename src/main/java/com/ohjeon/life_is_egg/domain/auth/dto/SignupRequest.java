package com.ohjeon.life_is_egg.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public class SignupRequest {
    private String email;
    private String password;
    private String nickname;
}