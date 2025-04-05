package com.example.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 상품 API 컨트롤러
 * 다양한 Cache Stampede 방지 기법을 테스트하기 위한 엔드포인트 제공
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    
    private final MutexLockCacheService mutexLockCacheService;
    private final CacheWarmingService cacheWarmingService;
    private final PerCacheService perCacheService;
    private final StaleWhileRevalidateService staleWhileRevalidateService;
    
    public ProductController(
            MutexLockCacheService mutexLockCacheService,
            CacheWarmingService cacheWarmingService,
            PerCacheService perCacheService,
            StaleWhileRevalidateService staleWhileRevalidateService) {
        this.mutexLockCacheService = mutexLockCacheService;
        this.cacheWarmingService = cacheWarmingService;
        this.perCacheService = perCacheService;
        this.staleWhileRevalidateService = staleWhileRevalidateService;
    }
    
    /**
     * Mutex Lock 방식으로 상품 조회
     */
    @GetMapping("/mutex/{id}")
    public Product getProductWithMutexLock(@PathVariable Long id) {
        log.info("Mutex Lock 방식 상품 조회 API 호출: ID {}", id);
        return mutexLockCacheService.getProductById(id);
    }
    
    /**
     * 캐시 워밍 방식으로 상품 조회
     */
    @GetMapping("/warming/{id}")
    public Product getProductWithCacheWarming(@PathVariable Long id) {
        log.info("캐시 워밍 방식 상품 조회 API 호출: ID {}", id);
        return cacheWarmingService.getProductById(id);
    }
    
    /**
     * PER 알고리즘 방식으로 상품 조회
     */
    @GetMapping("/per/{id}")
    public Product getProductWithPer(@PathVariable Long id) {
        log.info("PER 알고리즘 방식 상품 조회 API 호출: ID {}", id);
        return perCacheService.getProductById(id);
    }
    
    /**
     * Stale-While-Revalidate 방식으로 상품 조회
     */
    @GetMapping("/swr/{id}")
    public Product getProductWithStaleWhileRevalidate(@PathVariable Long id) {
        log.info("Stale-While-Revalidate 방식 상품 조회 API 호출: ID {}", id);
        return staleWhileRevalidateService.getProductById(id);
    }
    
    /**
     * 테스트를 위한 동시 요청 시뮬레이션 엔드포인트
     */
    @GetMapping("/simulate")
    public String simulateStampede(
            @RequestParam(defaultValue = "1") Long productId,
            @RequestParam(defaultValue = "mutex") String strategy,
            @RequestParam(defaultValue = "10") int concurrentRequests) {
        
        log.info("Cache Stampede 시뮬레이션 시작: 상품 ID {}, 전략 {}, 동시 요청 수 {}", 
                productId, strategy, concurrentRequests);
        
        // 여러 스레드에서 동시에 호출
        for (int i = 0; i < concurrentRequests; i++) {
            final int requestId = i;
            new Thread(() -> {
                try {
                    log.info("요청 #{} 시작: 전략 {}", requestId, strategy);
                    
                    switch (strategy) {
                        case "mutex":
                            mutexLockCacheService.getProductById(productId);
                            break;
                        case "warming":
                            cacheWarmingService.getProductById(productId);
                            break;
                        case "per":
                            perCacheService.getProductById(productId);
                            break;
                        case "swr":
                            staleWhileRevalidateService.getProductById(productId);
                            break;
                        default:
                            log.warn("알 수 없는 전략: {}", strategy);
                    }
                    
                    log.info("요청 #{} 완료: 전략 {}", requestId, strategy);
                } catch (Exception e) {
                    log.error("요청 #{} 실패: {}", requestId, e.getMessage(), e);
                }
            }).start();
        }
        
        return String.format("시뮬레이션 시작: 상품 ID %d, 전략 %s, 동시 요청 수 %d", 
                productId, strategy, concurrentRequests);
    }
}
