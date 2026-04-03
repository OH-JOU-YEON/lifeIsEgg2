package com.ohjeon.life_is_egg.domain.cheer.repository;

import com.ohjeon.life_is_egg.domain.auth.entity.User;
import com.ohjeon.life_is_egg.domain.cheer.entity.Cheer;
import com.ohjeon.life_is_egg.domain.post.entity.Post;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CheerRepository extends JpaRepository<Cheer, Long> {

    // 특정 일기의 전체 응원 조회 (트리 조립용 - flat으로 전체 가져옴)
    List<Cheer> findByPostOrderByCreatedAtAsc(Post post);

    // 내 일기에 달린 응원 수 (이번 달)
    @Query("""
            SELECT COUNT(c) FROM Cheer c
            WHERE c.post.user = :user
            AND c.createdAt BETWEEN :start AND :end
            """)
    long countCheersByPostOwner(@Param("user") User user, @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);
}