package com.ohjeon.life_is_egg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class LifeIsEggApplication {

    public static void main(String[] args) {
        SpringApplication.run(LifeIsEggApplication.class, args);
    }

}
