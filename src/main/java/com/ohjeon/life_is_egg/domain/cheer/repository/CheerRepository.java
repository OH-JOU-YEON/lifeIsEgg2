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

    List<Cheer> findByPostOrderByCreatedAtAsc(Post post);

    @Query("""
            SELECT c.post.id, COUNT(c)
            FROM Cheer c
            WHERE c.post.id IN :postIds
            AND c.deleted = false
            GROUP BY c.post.id
            """)
    List<Object[]> countByPostIds(@Param("postIds") List<Long> postIds);

    @Query("""
            SELECT COUNT(c) FROM Cheer c
            WHERE c.post.user = :user
            AND c.createdAt BETWEEN :start AND :end
            AND c.deleted = false
            """)
    long countCheersByPostOwner(@Param("user") User user, @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);

    long countByPostAndDeletedFalse(Post post);
}