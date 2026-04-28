package com.ohjeon.life_is_egg.domain.auth.service;

import com.ohjeon.life_is_egg.domain.auth.dto.LoginRequest;
import com.ohjeon.life_is_egg.domain.auth.dto.LoginResponse;
import com.ohjeon.life_is_egg.domain.auth.dto.SignupRequest;
import com.ohjeon.life_is_egg.domain.auth.entity.EmailVerificationToken;
import com.ohjeon.life_is_egg.domain.auth.entity.User;
import com.ohjeon.life_is_egg.domain.auth.repository.EmailVerificationTokenRepository;
import com.ohjeon.life_is_egg.domain.auth.repository.UserRepository;
import com.ohjeon.life_is_egg.global.jwt.JwtUtil;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .age(request.getAge())
                .build();
        userRepository.save(user);

        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        tokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 토큰입니다."));

        if (verificationToken.isExpired()) {
            throw new IllegalArgumentException("만료된 인증 토큰입니다.");
        }

        User user = verificationToken.getUser();
        user.verifyEmail();
        tokenRepository.delete(verificationToken);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        if (!user.isEmailVerified()) {
            throw new IllegalArgumentException("이메일 인증이 필요합니다.");
        }

        String token = jwtUtil.generateToken(user.getId());
        return new LoginResponse(token);
    }
}