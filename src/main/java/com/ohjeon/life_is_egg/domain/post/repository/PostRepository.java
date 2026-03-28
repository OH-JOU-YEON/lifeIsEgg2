package com.ohjeon.life_is_egg.domain.post.repository;

import com.ohjeon.life_is_egg.domain.auth.entity.User;
import com.ohjeon.life_is_egg.domain.post.entity.Post;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 내 일기 목록 (최신순, soft delete 제외)
    Page<Post> findByUserAndDeletedFalseOrderByCreatedAtDesc(User user, Pageable pageable);

    // 또래 피드 (PUBLIC, soft delete 제외, 특정 ID 제외, 나이 범위)
    @Query(value = """
            SELECT * FROM posts p
            JOIN users u ON p.user_id = u.id
            WHERE p.visibility = 'PUBLIC'
            AND p.deleted = false
            AND (:excludeIdsEmpty = true OR p.id NOT IN :excludeIds)
            AND u.age BETWEEN :minAge AND :maxAge
            ORDER BY RAND()
            LIMIT 10
            """, nativeQuery = true)
    List<Post> findFeedPosts(
            @Param("excludeIds") List<Long> excludeIds,
            @Param("excludeIdsEmpty") boolean excludeIdsEmpty,
            @Param("minAge") Byte minAge,
            @Param("maxAge") Byte maxAge
    );
}