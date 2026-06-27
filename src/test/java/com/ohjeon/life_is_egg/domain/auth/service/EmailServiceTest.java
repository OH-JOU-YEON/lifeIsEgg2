package com.ohjeon.life_is_egg.domain.auth.service;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "baseUrl", "http://localhost:5173");
    }

    @Test
    void 인증_메일_발송_성공() {
        String to = "test@example.com";
        String token = "test-token-uuid";

        emailService.sendVerificationEmail(to, token);

        verify(mailSender).send(argThat((SimpleMailMessage message) ->
                message.getTo()[0].equals(to) &&
                        message.getText().contains("http://localhost:5173/verify?token=" + token)
        ));
    }
}