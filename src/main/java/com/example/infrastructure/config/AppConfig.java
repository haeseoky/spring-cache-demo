package com.example.infrastructure.config;

import com.example.application.product.dto.ProductDto;
import com.example.application.product.service.CacheService;
import com.example.application.product.service.MutexLockCacheService;
import com.example.application.product.service.PerCacheService;
import com.example.application.product.service.StaleWhileRevalidateService;
import com.example.infrastructure.cache.RedisCacheService;
import com.example.infrastructure.lock.DistributedLock;
import com.example.infrastructure.lock.RedisLuaLock;
import com.example.infrastructure.lock.RedisSpinLock;
// com.example.infrastructure.lock.RedissonLock 클래스는 직접 구현했습니다.

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashMap;
import java.util.Map;

/**
 * 애플리케이션 설정 클래스
 */
@Configuration
public class AppConfig {
    
    /**
     * 분산락 구현체 맵 설정
     */
    @Bean
    public Map<String, DistributedLock> distributedLockMap(
            RedisSpinLock redisSpinLock,
            RedisLuaLock redisLuaLock) {
        Map<String, DistributedLock> locks = new HashMap<>();
        locks.put("redisSpinLock", redisSpinLock);
        locks.put("redisLuaLock", redisLuaLock);
        // redissonLock은 사용하지 않습니다.
        return locks;
    }
    
    /**
     * 기본 캐시 서비스 설정
     */
    @Bean
    @Primary
    public CacheService<ProductDto> primaryCacheService(RedisCacheService<ProductDto> redisCacheService) {
        return redisCacheService;
    }
    
    /**
     * ProductDto용 RedisCacheService 빈 생성
     */
    @Bean
    public RedisCacheService<ProductDto> productDtoRedisCacheService(org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate) {
        return new RedisCacheService<>(redisTemplate);
    }
    
    /**
     * 캐시 전략 맵 설정
     */
    @Bean
    public Map<String, CacheService<ProductDto>> cacheStrategyMap(
            MutexLockCacheService mutexLockCacheService,
            PerCacheService perCacheService,
            StaleWhileRevalidateService staleWhileRevalidateService,
            RedisCacheService<ProductDto> redisCacheService) {
        
        Map<String, CacheService<ProductDto>> strategies = new HashMap<>();
        strategies.put("mutex", mutexLockCacheService);
        strategies.put("per", perCacheService);
        strategies.put("swr", staleWhileRevalidateService);
        strategies.put("redis", redisCacheService);
        
        return strategies;
    }
}
