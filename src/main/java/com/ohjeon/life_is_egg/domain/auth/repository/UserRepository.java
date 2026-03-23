package com.ohjeon.life_is_egg.domain.auth.repository;

import com.ohjeon.life_is_egg.domain.auth.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email); // 중복 체크용
    Optional<User> findByEmail(String email);
}