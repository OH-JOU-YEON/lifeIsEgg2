package com.ohjeon.life_is_egg.domain.report.port;

import com.ohjeon.life_is_egg.domain.cheer.entity.Cheer;

public interface CheerLookupPort {

    Cheer getById(Long cheerId);
}