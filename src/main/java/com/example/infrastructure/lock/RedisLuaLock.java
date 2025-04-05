package com.example.infrastructure.lock;

import java.time.Duration;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

/**
 * Redis의 Lua 스크립트를 활용한 분산락 구현
 * 락 획득 및 해제를 원자적으로 처리
 */
@Component
public class RedisLuaLock implements DistributedLock {
    
    private static final Logger log = LoggerFactory.getLogger(RedisLuaLock.class);
    private static final String LOCK_PREFIX = "lock:lua:";
    
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<Boolean> acquireScript;
    private final RedisScript<Boolean> releaseScript;
    
    public RedisLuaLock(@Qualifier("lockStringRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        
        // 락 획득 스크립트
        DefaultRedisScript<Boolean> acquireScript = new DefaultRedisScript<>();
        acquireScript.setLocation(new ClassPathResource("scripts/acquire_lock.lua"));
        acquireScript.setResultType(Boolean.class);
        this.acquireScript = acquireScript;
        
        // 락 해제 스크립트
        DefaultRedisScript<Boolean> releaseScript = new DefaultRedisScript<>();
        releaseScript.setLocation(new ClassPathResource("scripts/release_lock.lua"));
        releaseScript.setResultType(Boolean.class);
        this.releaseScript = releaseScript;
    }
    
    @Override
    public boolean acquire(String key, Duration timeout) {
        String lockKey = LOCK_PREFIX + key;
        String lockValue = Thread.currentThread().getName() + ":" + System.currentTimeMillis();
        long timeoutMillis = timeout.toMillis();
        
        log.debug("Lua 락 획득 시도: {}", lockKey);
        
        Boolean result = redisTemplate.execute(
            acquireScript,
            Collections.singletonList(lockKey),
            lockValue, String.valueOf(timeoutMillis)
        );
        
        if (Boolean.TRUE.equals(result)) {
            log.debug("Lua 락 획득 성공: {}", lockKey);
            return true;
        } else {
            log.debug("Lua 락 획득 실패: {}", lockKey);
            return false;
        }
    }
    
    @Override
    public boolean release(String key) {
        String lockKey = LOCK_PREFIX + key;
        String lockValue = Thread.currentThread().getName() + ":" + System.currentTimeMillis();
        
        log.debug("Lua 락 해제 시도: {}", lockKey);
        
        Boolean result = redisTemplate.execute(
            releaseScript,
            Collections.singletonList(lockKey),
            lockValue
        );
        
        if (Boolean.TRUE.equals(result)) {
            log.debug("Lua 락 해제 성공: {}", lockKey);
            return true;
        } else {
            log.warn("Lua 락 해제 실패: {}", lockKey);
            return false;
        }
    }
}
