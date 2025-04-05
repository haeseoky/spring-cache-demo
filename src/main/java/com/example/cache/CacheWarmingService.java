package com.example.cache;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 캐시 워밍 방식으로 Cache Stampede 방지
 */
@Service
public class CacheWarmingService {
    
    private static final Logger log = LoggerFactory.getLogger(CacheWarmingService.class);
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductRepository productRepository;
    
    public CacheWarmingService(RedisTemplate<String, Object> redisTemplate, ProductRepository productRepository) {
        this.redisTemplate = redisTemplate;
        this.productRepository = productRepository;
    }
    
    /**
     * 상품 조회 - 캐시 워밍은 백그라운드에서 수행되므로 단순 캐시 조회 로직만 구현
     */
    public Product getProductById(Long id) {
        String cacheKey = "warming:product:" + id;
        
        log.info("캐시 워밍 방식으로 상품 ID {} 조회 시작", id);
        
        // 캐시에서 조회
        Product product = (Product) redisTemplate.opsForValue().get(cacheKey);
        
        if (product != null) {
            log.info("캐시 히트: 상품 ID {}", id);
            return product;
        }
        
        log.info("캐시 미스: 상품 ID {}", id);
        
        // 캐시 미스 시 DB에서 조회
        product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        
        // 캐시에 저장
        redisTemplate.opsForValue().set(cacheKey, product, Duration.ofMinutes(30));
        log.info("캐시 저장 완료: 상품 ID {}", id);
        
        return product;
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
            String cacheKey = "warming:product:" + id;
            
            // DB에서 데이터 조회
            Product product = productRepository.findById(id)
                    .orElse(null);
            
            if (product != null) {
                // 캐시에 저장 - 만료 시간은 30분으로 설정
                redisTemplate.opsForValue().set(cacheKey, product, Duration.ofMinutes(30));
                log.info("캐시 워밍 완료: 상품 ID {}", id);
            }
        }
        
        log.info("인기 상품 캐시 워밍 작업 완료");
    }
}
