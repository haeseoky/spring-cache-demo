package com.example.application.product.service;

import java.time.Duration;

import com.example.application.product.dto.ProductDto;
import com.example.domain.product.repository.ProductRepository;
import com.example.infrastructure.lock.DistributedLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Mutex Lock 방식으로 Cache Stampede 방지
 */
@Service
public class MutexLockCacheService implements CacheService<ProductDto> {
    
    private static final Logger log = LoggerFactory.getLogger(MutexLockCacheService.class);
    private static final String CACHE_KEY_PREFIX = "mutex:product:";
    private static final String LOCK_KEY_PREFIX = "mutex:lock:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);
    private static final Duration LOCK_TIMEOUT = Duration.ofSeconds(10);
    
    private final CacheService<ProductDto> delegateCacheService;
    private final ProductRepository productRepository;
    private final DistributedLock distributedLock;
    
    public MutexLockCacheService(
            CacheService<ProductDto> redisCacheService,
            ProductRepository productRepository,
            DistributedLock redisLuaLock) {
        this.delegateCacheService = redisCacheService;
        this.productRepository = productRepository;
        this.distributedLock = redisLuaLock;
    }
    
    @Override
    public ProductDto getOrLoad(String key, java.util.function.Supplier<ProductDto> loader, Duration ttl) {
        String cacheKey = CACHE_KEY_PREFIX + key;
        String lockKey = LOCK_KEY_PREFIX + key;
        
        log.info("Mutex Lock 방식으로 키 {} 조회 시작", key);
        
        // 1. 캐시에서 먼저 조회
        ProductDto value = delegateCacheService.getOrLoad(cacheKey, () -> null, ttl);
        if (value != null) {
            log.info("캐시 히트: 키 {}", key);
            return value;
        }
        
        log.info("캐시 미스: 키 {}", key);
        
        // 2. 락 획득 시도
        try {
            return distributedLock.executeWithLock(lockKey, LOCK_TIMEOUT, () -> {
                // 3. 락 획득 후 캐시 다시 확인 (다른 스레드가 이미 업데이트했을 수 있음)
                ProductDto cachedValue = delegateCacheService.getOrLoad(cacheKey, () -> null, ttl);
                if (cachedValue != null) {
                    log.info("락 획득 후 캐시 확인 히트: 키 {}", key);
                    return cachedValue;
                }
                
                // 4. 원본 로더 실행
                log.info("데이터 로드 시작: 키 {}", key);
                ProductDto loadedValue = loader.get();
                
                // 5. 캐시에 저장
                if (loadedValue != null) {
                    delegateCacheService.put(cacheKey, loadedValue, ttl);
                    log.info("캐시 저장 완료: 키 {}", key);
                }
                
                return loadedValue;
            });
        } catch (Exception e) {
            log.error("락 처리 중 오류 발생: {}", e.getMessage(), e);
            // 락 획득 실패 시 기본 로더 실행
            return loader.get();
        }
    }

    /**
     * 상품 ID로 조회하는 편의 메서드
     */
    public ProductDto getProductById(Long id) {
        return getOrLoad(id.toString(), () -> 
            productRepository.findById(id)
                .map(ProductDto::fromEntity)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id)),
            CACHE_TTL
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
