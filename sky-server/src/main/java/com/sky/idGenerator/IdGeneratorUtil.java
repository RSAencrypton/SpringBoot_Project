package com.sky.idGenerator;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class IdGeneratorUtil {

    private static final long BIG_TIME = 1640995200L;
    private static final int OFFSET_BIT = 32;

    @Autowired
    private RedisTemplate redisTemplate;

    public long nextId(String keyPrefix) {
        LocalDateTime now = LocalDateTime.now();
        long time = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = time - BIG_TIME;

        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        long count = redisTemplate.opsForValue().increment(keyPrefix + ":" + date);

        return timestamp << OFFSET_BIT | count;
    }
}
