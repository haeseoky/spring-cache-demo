package com.example.application.product.service;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.example.application.product.dto.ProductDto;
import com.example.domain.product.repository.ProductRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * PER(Probabilistic Early Recomputation) 방식으로 Cache Stampede 방지
 */
@Service
public class PerCacheService implements CacheService<ProductDto> {
    
    private static final Logger log = LoggerFactory.getLogger(PerCacheService.class);
    private static final Random random = new Random();
    private static final String CACHE_KEY_PREFIX = "per:";
    private static final Duration DEFAULT_CACHE_TTL = Duration.ofMinutes(10);
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductRepository productRepository;
    private final CacheService<ProductDto> delegateCacheService;
    
    public PerCacheService(
            RedisTemplate<String, Object> redisTemplate,
            ProductRepository productRepository,
            CacheService<ProductDto> redisCacheService) {
        this.redisTemplate = redisTemplate;
        this.productRepository = productRepository;
        this.delegateCacheService = redisCacheService;
    }
    
    @Override
    public ProductDto getOrLoad(String key, Supplier<ProductDto> loader, Duration ttl) {
        String cacheKey = CACHE_KEY_PREFIX + key;
        
        log.info("PER 방식으로 키 {} 조회 시작", key);
        
        // 1. 캐시에서 조회
        @SuppressWarnings("unchecked")
        ProductDto value = (ProductDto) redisTemplate.opsForValue().get(cacheKey);
        
        // 2. 캐시에 값이 있는 경우
        if (value != null) {
            log.info("캐시 히트: 키 {}", key);
            
            // 3. 캐시 만료 시간 조회
            Long remainingTtl = redisTemplate.getExpire(cacheKey, TimeUnit.MILLISECONDS);
            
            if (remainingTtl != null && remainingTtl > 0) {
                // 4. 남은 TTL 비율 계산
                double remainingRatio = (double) remainingTtl / ttl.toMillis();
                log.info("남은 TTL 비율: {}, 키 {}", String.format("%.2f", remainingRatio), key);
                
                // 5. 남은 시간이 적을수록 재계산 확률이 높아짐
                if (remainingRatio < 0.2 && shouldRefreshCache(remainingRatio)) {
                    log.info("PER 알고리즘에 의한 캐시 조기 갱신 시작: 키 {}", key);
                    
                    // 6. 비동기로 캐시 갱신
                    CompletableFuture.runAsync(() -> {
                        ProductDto refreshedValue = loader.get();
                        if (refreshedValue != null) {
                            delegateCacheService.put(cacheKey, refreshedValue, ttl);
                            log.info("비동기 캐시 갱신 완료: 키 {}", key);
                        }
                    });
                }
            }
            
            return value;
        }
        
        log.info("캐시 미스: 키 {}", key);
        
        // 7. 캐시에 값이 없는 경우 동기적으로 조회
        ProductDto loadedValue = loader.get();
        if (loadedValue != null) {
            delegateCacheService.put(cacheKey, loadedValue, ttl);
            log.info("캐시 저장 완료: 키 {}", key);
        }
        
        return loadedValue;
    }
    
    /**
     * 캐시 갱신 확률 계산
     * 남은 TTL 비율이 낮을수록 갱신 확률이 높아짐
     */
    private boolean shouldRefreshCache(double remainingRatio) {
        // TTL이 20% 남았을 때 20% 확률로 갱신, 10% 남았을 때 50% 확률로 갱신
        double refreshProbability = Math.min(1.0, (0.2 - remainingRatio) * 5);
        
        // 0~1 사이의 난수 생성하여 확률 계산
        double randomValue = random.nextDouble();
        boolean shouldRefresh = randomValue < refreshProbability;
        
        log.info("갱신 확률: {}, 난수: {}, 갱신 여부: {}", 
                String.format("%.2f", refreshProbability), 
                String.format("%.2f", randomValue), 
                shouldRefresh);
        
        return shouldRefresh;
    }
    
    /**
     * 상품 ID로 조회하는 편의 메서드
     */
    public ProductDto getProductById(Long id) {
        return getOrLoad(id.toString(), () -> 
            productRepository.findById(id)
                .map(ProductDto::fromEntity)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id)),
            DEFAULT_CACHE_TTL
        );
    }
    
    @Override
    public void put(String key, ProductDto value, Duration ttl) {
        delegateCacheService.put(CACHE_KEY_PREFIX + key, value, ttl);
    }
    
    @Override
    public void evict(String key) {
        delegateCacheService.evict(CACHE_KEY_PREFIX + key);
    }
    
    @Override
    public boolean exists(String key) {
        return delegateCacheService.exists(CACHE_KEY_PREFIX + key);
    }
}
