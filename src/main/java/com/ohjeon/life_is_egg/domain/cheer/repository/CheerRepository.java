package com.ohjeon.life_is_egg.domain.cheer.repository;

import com.ohjeon.life_is_egg.domain.cheer.entity.Cheer;
import com.ohjeon.life_is_egg.domain.post.entity.Post;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheerRepository extends JpaRepository<Cheer, Long> {

    // 특정 일기의 전체 응원 조회 (트리 조립용 - flat으로 전체 가져옴)
    List<Cheer> findByPostOrderByCreatedAtAsc(Post post);
}