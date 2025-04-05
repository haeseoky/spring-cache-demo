package com.example.infrastructure.cache;

import com.example.application.product.service.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Redis 기반 캐시 서비스 구현체
 */
@Service
public class RedisCacheService<T> implements CacheService<T> {
    
    private static final Logger log = LoggerFactory.getLogger(RedisCacheService.class);
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public RedisCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public T getOrLoad(String key, Supplier<T> loader, Duration ttl) {
        // 캐시에서 조회
        @SuppressWarnings("unchecked")
        T cachedValue = (T) redisTemplate.opsForValue().get(key);
        
        if (cachedValue != null) {
            log.debug("캐시 히트: {}", key);
            return cachedValue;
        }
        
        log.debug("캐시 미스: {}", key);
        
        // 캐시에 없으면 로드 함수 실행
        T loadedValue = loader.get();
        
        if (loadedValue != null) {
            // 캐시에 저장
            put(key, loadedValue, ttl);
        }
        
        return loadedValue;
    }
    
    @Override
    public void put(String key, T value, Duration ttl) {
        if (value == null) {
            log.debug("캐시 저장 무시 (널 값): {}", key);
            return;
        }
        
        redisTemplate.opsForValue().set(key, value, ttl);
        log.debug("캐시 저장: {} (TTL: {}s)", key, ttl.getSeconds());
    }
    
    @Override
    public void evict(String key) {
        Boolean deleted = redisTemplate.delete(key);
        log.debug("캐시 제거: {} (성공: {})", key, deleted);
    }
    
    @Override
    public boolean exists(String key) {
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }
}
