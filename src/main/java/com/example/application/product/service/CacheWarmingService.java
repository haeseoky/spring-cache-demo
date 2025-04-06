package com.example.application.product.service;

import com.example.application.product.dto.ProductDto;
import com.example.domain.product.entity.Product;
import com.example.domain.product.repository.ProductRepository;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 캐시 워밍 방식으로 Cache Stampede 방지
 */
@Service
public class CacheWarmingService {
    
    private static final Logger log = LoggerFactory.getLogger(CacheWarmingService.class);
    private static final String CACHE_KEY_PREFIX = "warming:product:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);
    
    private final ProductRepository productRepository;
    private final CacheService<ProductDto> cacheService;
    
    public CacheWarmingService(
            ProductRepository productRepository,
            CacheService<ProductDto> cacheService) {
        this.productRepository = productRepository;
        this.cacheService = cacheService;
    }
    
    /**
     * 상품 조회 - 캐시 워밍은 백그라운드에서 수행되므로 단순 캐시 조회 로직만 구현
     */
    public ProductDto getProductById(Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        
        log.info("캐시 워밍 방식으로 상품 ID {} 조회 시작", id);
        
        return cacheService.getOrLoad(cacheKey, () -> 
            productRepository.findById(id)
                .map(ProductDto::fromEntity)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id)),
            CACHE_TTL
        );
    }
    
    /**
     * 인기 상품 캐시 워밍 작업
     * 스케줄러를 사용하여 주기적으로 실행
     */
//    @Scheduled(fixedRate = 60000) // 1분마다 실행 (테스트를 위해 짧게 설정)
    public void warmPopularProductsCache() {
        log.info("인기 상품 캐시 워밍 작업 시작");
        
        // 인기 상품 ID 목록 조회
        List<Long> popularProductIds = productRepository.findTopProductIds(20);
        
        for (Long id : popularProductIds) {
            String cacheKey = CACHE_KEY_PREFIX + id;
            
            // DB에서 데이터 조회
            productRepository.findById(id)
                .map(ProductDto::fromEntity)
                .ifPresent(productDto -> {
                    // 캐시에 저장
                    cacheService.put(cacheKey, productDto, CACHE_TTL);
                    log.info("캐시 워밍 완료: 상품 ID {}", id);
                });
        }
        
        log.info("인기 상품 캐시 워밍 작업 완료");
    }
}
