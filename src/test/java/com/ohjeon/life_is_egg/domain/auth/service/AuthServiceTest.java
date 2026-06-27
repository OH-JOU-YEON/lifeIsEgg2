package com.ohjeon.life_is_egg.domain.auth.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ohjeon.life_is_egg.domain.auth.dto.LoginRequest;
import com.ohjeon.life_is_egg.domain.auth.dto.LoginResponse;
import com.ohjeon.life_is_egg.domain.auth.dto.SignupRequest;
import com.ohjeon.life_is_egg.domain.auth.entity.User;
import com.ohjeon.life_is_egg.domain.auth.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void 회원가입_성공() {
        SignupRequest request = new SignupRequest("test@test2.com", "password1234", "테스터2", null);
        authService.signup(request);

        assertTrue(userRepository.existsByEmail("test@test2.com"));
    }

    @Test
    void 이메일_중복_회원가입_실패() {
        SignupRequest request = new SignupRequest("test@test2.com", "password1234", "테스터2", null);
        authService.signup(request);

        assertThrows(IllegalArgumentException.class, () -> authService.signup(request));
    }


    @Test
    void 로그인_성공() {
        // 회원가입
        authService.signup(new SignupRequest("login@test.com", "password1234", "로그인테스터", null));

        // 이메일 인증 처리 (실제로는 메일 링크 클릭 시 일어나는 일)
        User user = userRepository.findByEmail("login@test.com").orElseThrow();
        user.verifyEmail();
        userRepository.save(user);

        // 로그인
        LoginRequest request = new LoginRequest("login@test.com", "password1234");
        LoginResponse response = authService.login(request);

        assertNotNull(response.getAccessToken());
    }

    @Test
    void 비밀번호_불일치_로그인_실패() {
        authService.signup(new SignupRequest("login@test.com", "password1234", "로그인테스터", null));

        LoginRequest request = new LoginRequest("login@test.com", "wrongpassword");

        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }
}