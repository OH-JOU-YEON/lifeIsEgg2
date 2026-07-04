package com.ohjeon.life_is_egg.domain.post.port;

import com.ohjeon.life_is_egg.domain.post.entity.Post;
import java.util.List;
import java.util.Map;

public interface CheerCountPort {

    Map<Long, Long> countByPostIds(List<Long> postIds);

    long countByPost(Post post);
}