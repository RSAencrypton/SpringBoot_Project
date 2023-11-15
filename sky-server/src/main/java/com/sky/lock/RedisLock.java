package com.sky.lock;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class RedisLock implements ILock{

    private String name;
    private RedisTemplate redisTemplate;

    private static final String ID_PREFIX = UUID.randomUUID().toString().replace("-", "") + "-";
    private static final DefaultRedisScript<Long> UNLOCK_LUA_SCRIPT;
    static {
        UNLOCK_LUA_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_LUA_SCRIPT.setLocation(new ClassPathResource("AtomReleaseLock.lua"));
        UNLOCK_LUA_SCRIPT.setResultType(Long.class);
    }

    public RedisLock(String name, RedisTemplate redisTemplate) {
        this.name = name;
        this.redisTemplate = redisTemplate;
    }


    @Override
    public boolean TryLock(Long TTL) {
        String id = ID_PREFIX + Thread.currentThread().getId();
        Boolean res =  redisTemplate.opsForValue().setIfAbsent(name,id, TTL, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(res);
    }

//    @Override
//    public void unLock() {
//        String id = ID_PREFIX + Thread.currentThread().getId();
//        String lockId = redisTemplate.opsForValue().get(name).toString();
//
//        if (id.equals(lockId)) {
//            redisTemplate.delete(name);
//        }
//    }

    @Override
    public void unLock() {
        redisTemplate.execute(UNLOCK_LUA_SCRIPT, Collections.singletonList(name),
                ID_PREFIX + Thread.currentThread().getId());
    }
}
