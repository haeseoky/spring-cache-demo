package com.example.infrastructure.lock;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis를 이용한 스핀락 구현
 * 락을 획득할 때까지 계속해서 시도하는 방식
 */
@Component
public class RedisSpinLock implements DistributedLock {
    
    private static final Logger log = LoggerFactory.getLogger(RedisSpinLock.class);
    private static final String LOCK_PREFIX = "lock:spin:";
    private static final long SPIN_INTERVAL_MS = 100; // 재시도 간격 (밀리초)
    
    private final RedisTemplate<String, String> redisTemplate;
    
    public RedisSpinLock(@Qualifier("lockStringRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public boolean acquire(String key, Duration timeout) {
        String lockKey = LOCK_PREFIX + key;
        String lockValue = Thread.currentThread().getName() + ":" + System.currentTimeMillis();
        long startTime = System.currentTimeMillis();
        long timeoutMillis = timeout.toMillis();
        
        log.debug("스핀락 획득 시도: {}", lockKey);
        
        // 타임아웃까지 락 획득 시도를 반복
        while (true) {

            long l = System.currentTimeMillis() - startTime;
            if(l >=  timeoutMillis) break;
            log.debug("스핀락 획득 시도: {}ms", l);

            boolean acquired = Boolean.TRUE.equals(
                redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, timeout)
            );
            
            if (acquired) {
                log.debug("스핀락 획득 성공: {}", lockKey);
                return true;
            }
            
            // 스핀 대기
            try {
                TimeUnit.MILLISECONDS.sleep(SPIN_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("스핀락 대기 중 인터럽트 발생: {}", lockKey);
                return false;
            }
        }
        
        log.debug("스핀락 획득 타임아웃: {}", lockKey);
        return false;
    }
    
    @Override
    public boolean release(String key) {
        String lockKey = LOCK_PREFIX + key;
        boolean released = Boolean.TRUE.equals(redisTemplate.delete(lockKey));
        
        if (released) {
            log.debug("스핀락 해제 성공: {}", lockKey);
        } else {
            log.warn("스핀락 해제 실패: {}", lockKey);
        }
        
        return released;
    }
}
