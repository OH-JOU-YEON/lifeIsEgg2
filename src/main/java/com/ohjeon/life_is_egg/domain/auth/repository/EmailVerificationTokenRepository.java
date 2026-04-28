package com.ohjeon.life_is_egg.domain.auth.repository;

import com.ohjeon.life_is_egg.domain.auth.entity.EmailVerificationToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByToken(String token);

    void deleteByUser(com.ohjeon.life_is_egg.domain.auth.entity.User user);
}