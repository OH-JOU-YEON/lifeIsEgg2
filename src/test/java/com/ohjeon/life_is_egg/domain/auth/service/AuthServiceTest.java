package com.ohjeon.life_is_egg.domain.auth.service;

import static org.junit.jupiter.api.Assertions.*;

import com.ohjeon.life_is_egg.domain.auth.dto.SignupRequest;
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
        SignupRequest request = new SignupRequest("test@test2.com", "password1234", "테스터2");
        authService.signup(request);

        assertTrue(userRepository.existsByEmail("test@test2.com"));
    }

    @Test
    void 이메일_중복_회원가입_실패() {
        SignupRequest request = new SignupRequest("test@test2.com", "password1234", "테스터2");
        authService.signup(request);

        assertThrows(IllegalArgumentException.class, () -> authService.signup(request));
    }
}