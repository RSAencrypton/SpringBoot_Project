package com.sky.config;


import io.lettuce.core.RedisClient;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    private static final String LOCK_PASSWORD = "zhu602894";
    @Bean
    public RedissonClient redissonManager() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://localhost:6379").setPassword(LOCK_PASSWORD);

        return Redisson.create(config);
    }
}
