package com.ohjeon.life_is_egg.domain.report.port;

import com.ohjeon.life_is_egg.domain.post.entity.Post;

public interface PostLookupPort {

    Post getById(Long postId);
}