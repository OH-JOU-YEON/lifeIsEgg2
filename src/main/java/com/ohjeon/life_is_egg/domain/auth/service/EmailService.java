package com.ohjeon.life_is_egg.domain.auth.service;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendVerificationEmail(String to, String token) {
        String link = baseUrl + "/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[삶은달걀] 이메일 인증");
        message.setText("아래 링크를 클릭하면 이메일 인증이 완료됩니다.\n\n" + link + "\n\n링크는 24시간 후 만료됩니다.");

        mailSender.send(message);
    }
}
