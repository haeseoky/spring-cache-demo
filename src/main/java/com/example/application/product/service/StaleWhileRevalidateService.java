package com.example.application.product.service;

import com.example.application.product.dto.ProductDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Stale-While-Revalidate 패턴으로 Cache Stampede 방지
 * 만료된 데이터를 반환하면서 백그라운드에서 새로운 데이터를 갱신
 */
@Service
public class StaleWhileRevalidateService implements CacheService<ProductDto> {
    
    private static final Logger log = LoggerFactory.getLogger(StaleWhileRevalidateService.class);
    private static final String FRESH_CACHE_PREFIX = "swr:fresh:";
    private static final String STALE_CACHE_PREFIX = "swr:stale:";
    private static final Duration STALE_TTL = Duration.ofHours(1);
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public StaleWhileRevalidateService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public ProductDto getOrLoad(String key, Supplier<ProductDto> loader, Duration ttl) {
        String freshKey = FRESH_CACHE_PREFIX + key;
        String staleKey = STALE_CACHE_PREFIX + key;
        
        log.debug("SWR 캐시 조회: {}", key);
        
        // 1. 신선한 캐시에서 조회
        ProductDto product = (ProductDto) redisTemplate.opsForValue().get(freshKey);
        
        if (product != null) {
            log.debug("신선한 캐시 히트: {}", key);
            return product;
        }
        
        log.debug("신선한 캐시 미스: {}", key);
        
        // 2. 오래된 캐시에서 조회
        ProductDto staleProduct = (ProductDto) redisTemplate.opsForValue().get(staleKey);
        
        // 3. 오래된 캐시에서 데이터를 찾은 경우
        if (staleProduct != null) {
            log.debug("오래된 캐시 히트, 백그라운드 갱신 시작: {}", key);
            
            // 4. 백그라운드에서 캐시 갱신
            CompletableFuture.runAsync(() -> {
                refreshCache(key, freshKey, staleKey, loader, ttl);
            });
            
            return staleProduct;
        }
        
        log.debug("오래된 캐시 미스, 동기적 로드 시작: {}", key);
        
        // 5. 캐시에 아무것도 없는 경우 동기적으로 로드 (첫 로딩)
        return refreshCache(key, freshKey, staleKey, loader, ttl);
    }
    
    /**
     * 캐시 갱신
     */
    private ProductDto refreshCache(String key, String freshKey, String staleKey, 
                                  Supplier<ProductDto> loader, Duration ttl) {
        log.debug("캐시 갱신 시작: {}", key);
        
        // 데이터 로드
        ProductDto product = loader.get();
        
        if (product != null) {
            // 신선한 캐시에 저장
            put(freshKey, product, ttl);
            
            // 오래된 캐시에 저장 (더 긴 TTL)
            put(staleKey, product, STALE_TTL);
        }
        
        return product;
    }
    
    @Override
    public void put(String key, ProductDto value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
        log.debug("캐시 저장: {} (TTL: {}s)", key, ttl.getSeconds());
    }
    
    @Override
    public void evict(String key) {
        String freshKey = FRESH_CACHE_PREFIX + key;
        String staleKey = STALE_CACHE_PREFIX + key;
        
        redisTemplate.delete(freshKey);
        redisTemplate.delete(staleKey);
        
        log.debug("SWR 캐시 제거: {}", key);
    }
    
    @Override
    public boolean exists(String key) {
        String freshKey = FRESH_CACHE_PREFIX + key;
        String staleKey = STALE_CACHE_PREFIX + key;
        
        Boolean freshExists = redisTemplate.hasKey(freshKey);
        Boolean staleExists = redisTemplate.hasKey(staleKey);
        
        return Boolean.TRUE.equals(freshExists) || Boolean.TRUE.equals(staleExists);
    }
}
