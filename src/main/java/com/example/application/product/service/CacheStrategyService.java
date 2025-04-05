package com.example.application.product.service;

import java.time.Duration;
import java.util.Map;

import com.example.application.product.dto.ProductDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 다양한 캐시 전략을 선택적으로 사용할 수 있는 통합 서비스
 */
@Service
public class CacheStrategyService {
    
    private static final Logger log = LoggerFactory.getLogger(CacheStrategyService.class);
    
    // 캐시 전략 구현체 맵
    private final Map<String, CacheService<ProductDto>> cacheStrategies;
    
    // 각 전략별 서비스
    private final MutexLockCacheService mutexLockService;
    private final CacheWarmingService warmingService;
    private final PerCacheService perService;
    private final StaleWhileRevalidateService swrService;
    
    public CacheStrategyService(
            MutexLockCacheService mutexLockService,
            CacheWarmingService warmingService,
            PerCacheService perService,
            StaleWhileRevalidateService swrService,
            Map<String, CacheService<ProductDto>> cacheStrategies) {
        this.mutexLockService = mutexLockService;
        this.warmingService = warmingService;
        this.perService = perService;
        this.swrService = swrService;
        this.cacheStrategies = cacheStrategies;
    }
    
    /**
     * 뮤텍스 락 방식으로 상품 조회
     */
    public ProductDto getProductWithMutexLock(Long id) {
        log.info("뮤텍스 락 방식으로 상품 조회: ID={}", id);
        return mutexLockService.getProductById(id);
    }
    
    /**
     * 캐시 워밍 방식으로 상품 조회
     */
    public ProductDto getProductWithCacheWarming(Long id) {
        log.info("캐시 워밍 방식으로 상품 조회: ID={}", id);
        return warmingService.getProductById(id);
    }
    
    /**
     * PER 알고리즘 방식으로 상품 조회
     */
    public ProductDto getProductWithPer(Long id) {
        log.info("PER 알고리즘 방식으로 상품 조회: ID={}", id);
        return perService.getProductById(id);
    }
    
    /**
     * Stale-While-Revalidate 방식으로 상품 조회
     */
    public ProductDto getProductWithStaleWhileRevalidate(Long id) {
        log.info("Stale-While-Revalidate 방식으로 상품 조회: ID={}", id);
        return swrService.getOrLoad("product:" + id, () -> null, Duration.ofMinutes(5));
    }
    
    /**
     * 전략 이름으로 캐시 서비스 가져오기
     */
    public CacheService<ProductDto> getStrategy(String strategyName) {
        return cacheStrategies.getOrDefault(strategyName, swrService);
    }
}
