package com.example.cache;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * PER(Probabilistic Early Recomputation) 방식으로 Cache Stampede 방지
 */
@Service
public class PerCacheService {
    
    private static final Logger log = LoggerFactory.getLogger(PerCacheService.class);
    private static final Random random = new Random();
    
    // 캐시 TTL 설정 (10분)
    private static final long CACHE_TTL_MS = 600000;
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductRepository productRepository;
    
    public PerCacheService(RedisTemplate<String, Object> redisTemplate, ProductRepository productRepository) {
        this.redisTemplate = redisTemplate;
        this.productRepository = productRepository;
    }
    
    /**
     * PER 알고리즘을 이용한 상품 조회
     * 캐시 만료 시간이 가까워질수록 확률적으로 미리 갱신
     */
    public Product getProductById(Long id) {
        String cacheKey = "per:product:" + id;
        
        log.info("PER 방식으로 상품 ID {} 조회 시작", id);
        
        // 1. 캐시에서 조회
        Product product = (Product) redisTemplate.opsForValue().get(cacheKey);
        
        // 2. 캐시에 값이 있는 경우
        if (product != null) {
            log.info("캐시 히트: 상품 ID {}", id);
            
            // 3. 캐시 만료 시간 조회
            Long ttl = redisTemplate.getExpire(cacheKey, TimeUnit.MILLISECONDS);
            
            if (ttl != null && ttl > 0) {
                // 4. 남은 TTL 비율 계산
                double remainingRatio = (double) ttl / CACHE_TTL_MS;
                log.info("남은 TTL 비율: {}, 상품 ID {}", String.format("%.2f", remainingRatio), id);
                
                // 5. 남은 시간이 적을수록 재계산 확률이 높아짐
                if (remainingRatio < 0.2 && shouldRefreshCache(remainingRatio)) {
                    log.info("PER 알고리즘에 의한 캐시 조기 갱신 시작: 상품 ID {}", id);
                    
                    // 6. 비동기로 캐시 갱신
                    CompletableFuture.runAsync(() -> {
                        refreshCache(id, cacheKey);
                    });
                }
            }
            
            return product;
        }
        
        log.info("캐시 미스: 상품 ID {}", id);
        
        // 7. 캐시에 값이 없는 경우 동기적으로 조회
        return refreshCache(id, cacheKey);
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
     * 캐시 갱신
     */
    private Product refreshCache(Long id, String cacheKey) {
        log.info("DB에서 상품 조회 시작: ID {}", id);
        
        // DB에서 데이터 조회
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        
        // 캐시에 저장 - TTL 10분
        redisTemplate.opsForValue().set(cacheKey, product, Duration.ofMillis(CACHE_TTL_MS));
        log.info("캐시 저장 완료: 상품 ID {}", id);
        
        return product;
    }
}
