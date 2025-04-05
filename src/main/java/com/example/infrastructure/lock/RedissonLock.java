package com.example.infrastructure.lock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Redisson 라이브러리를 활용한 분산락 구현
 * Redisson이 제공하는 RLock 인터페이스를 활용
 */
@Component
public class RedissonLock implements DistributedLock {
    
    private static final Logger log = LoggerFactory.getLogger(RedissonLock.class);
    private static final String LOCK_PREFIX = "lock:redisson:";
    
    private final RedissonClient redissonClient;
    
    public RedissonLock(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }
    
    @Override
    public boolean acquire(String key, Duration timeout) {
        String lockKey = LOCK_PREFIX + key;
        RLock lock = redissonClient.getLock(lockKey);
        
        log.debug("Redisson 락 획득 시도: {}", lockKey);
        
        try {
            boolean acquired = lock.tryLock(timeout.toMillis(), timeout.toMillis(), TimeUnit.MILLISECONDS);
            
            if (acquired) {
                log.debug("Redisson 락 획득 성공: {}", lockKey);
            } else {
                log.debug("Redisson 락 획득 실패: {}", lockKey);
            }
            
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Redisson 락 획득 중 인터럽트 발생: {}", lockKey);
            return false;
        }
    }
    
    @Override
    public boolean release(String key) {
        String lockKey = LOCK_PREFIX + key;
        RLock lock = redissonClient.getLock(lockKey);
        
        log.debug("Redisson 락 해제 시도: {}", lockKey);
        
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.debug("Redisson 락 해제 성공: {}", lockKey);
            return true;
        } else {
            log.warn("Redisson 락 해제 실패 (현재 스레드가 소유하지 않음): {}", lockKey);
            return false;
        }
    }
}
