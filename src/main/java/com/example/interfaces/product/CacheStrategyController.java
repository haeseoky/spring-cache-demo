package com.example.interfaces.product;

import com.example.application.product.dto.ProductDto;
import com.example.application.product.service.CacheStrategyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 다양한 캐시 전략을 테스트할 수 있는 컨트롤러
 */
@RestController
@RequestMapping("/api/cache-strategies")
public class CacheStrategyController {
    
    private static final Logger log = LoggerFactory.getLogger(CacheStrategyController.class);
    
    private final CacheStrategyService cacheStrategyService;
    
    public CacheStrategyController(CacheStrategyService cacheStrategyService) {
        this.cacheStrategyService = cacheStrategyService;
    }
    
    /**
     * 뮤텍스 락 방식으로 상품 조회
     */
    @GetMapping("/mutex/{id}")
    public ResponseEntity<ProductDto> getProductWithMutexLock(@PathVariable Long id) {
        log.info("뮤텍스 락 방식 상품 조회 API 호출: ID={}", id);
        return ResponseEntity.ok(cacheStrategyService.getProductWithMutexLock(id));
    }
    
    /**
     * 캐시 워밍 방식으로 상품 조회
     */
    @GetMapping("/warming/{id}")
    public ResponseEntity<ProductDto> getProductWithCacheWarming(@PathVariable Long id) {
        log.info("캐시 워밍 방식 상품 조회 API 호출: ID={}", id);
        return ResponseEntity.ok(cacheStrategyService.getProductWithCacheWarming(id));
    }
    
    /**
     * PER 알고리즘 방식으로 상품 조회
     */
    @GetMapping("/per/{id}")
    public ResponseEntity<ProductDto> getProductWithPer(@PathVariable Long id) {
        log.info("PER 알고리즘 방식 상품 조회 API 호출: ID={}", id);
        return ResponseEntity.ok(cacheStrategyService.getProductWithPer(id));
    }
    
    /**
     * Stale-While-Revalidate 방식으로 상품 조회
     */
    @GetMapping("/swr/{id}")
    public ResponseEntity<ProductDto> getProductWithStaleWhileRevalidate(@PathVariable Long id) {
        log.info("Stale-While-Revalidate 방식 상품 조회 API 호출: ID={}", id);
        return ResponseEntity.ok(cacheStrategyService.getProductWithStaleWhileRevalidate(id));
    }
    
    /**
     * 전략 이름으로 상품 조회 (범용 엔드포인트)
     */
    @GetMapping("/{strategy}/{id}")
    public ResponseEntity<ProductDto> getProductWithStrategy(
            @PathVariable String strategy,
            @PathVariable Long id) {
        log.info("{} 전략으로 상품 조회 API 호출: ID={}", strategy, id);
        
        // 전략에 따라 다른 처리
        switch (strategy) {
            case "mutex":
                return ResponseEntity.ok(cacheStrategyService.getProductWithMutexLock(id));
            case "warming":
                return ResponseEntity.ok(cacheStrategyService.getProductWithCacheWarming(id));
            case "per":
                return ResponseEntity.ok(cacheStrategyService.getProductWithPer(id));
            case "swr":
                return ResponseEntity.ok(cacheStrategyService.getProductWithStaleWhileRevalidate(id));
            default:
                throw new IllegalArgumentException("Unknown cache strategy: " + strategy);
        }
    }
}
