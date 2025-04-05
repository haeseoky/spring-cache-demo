package com.example.cache;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Mutex Lock 방식으로 Cache Stampede 방지
 */
@Service
public class MutexLockCacheService {
    
    private static final Logger log = LoggerFactory.getLogger(MutexLockCacheService.class);
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductRepository productRepository;
    
    public MutexLockCacheService(RedisTemplate<String, Object> redisTemplate, ProductRepository productRepository) {
        this.redisTemplate = redisTemplate;
        this.productRepository = productRepository;
    }
    
    /**
     * Mutex Lock 방식으로 상품 조회
     * 캐시 미스 시 락을 획득한 스레드만 DB 조회 수행
     */
    public Product getProductById(Long id) {
        String cacheKey = "mutex:product:" + id;
        String lockKey = "mutex:lock:" + cacheKey;
        
        log.info("Mutex Lock 방식으로 상품 ID {} 조회 시작", id);
        
        // 1. 캐시에서 먼저 조회
        Product product = (Product) redisTemplate.opsForValue().get(cacheKey);
        if (product != null) {
            log.info("캐시 히트: 상품 ID {}", id);
            return product;
        }
        
        log.info("캐시 미스: 상품 ID {}", id);
        
        // 2. 락 획득 시도
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", Duration.ofSeconds(10));
        if (Boolean.TRUE.equals(acquired)) {
            try {
                log.info("락 획득 성공: {}", lockKey);
                
                // 3. 락 획득 후 캐시 다시 확인 (다른 스레드가 이미 업데이트했을 수 있음)
                product = (Product) redisTemplate.opsForValue().get(cacheKey);
                if (product != null) {
                    log.info("락 획득 후 캐시 확인 히트: 상품 ID {}", id);
                    return product;
                }
                
                // 4. DB에서 데이터 조회
                log.info("DB에서 상품 조회 시작: ID {}", id);
                product = productRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Product not found: " + id));
                
                // 5. 캐시에 저장
                redisTemplate.opsForValue().set(cacheKey, product, Duration.ofMinutes(5));
                log.info("캐시 저장 완료: 상품 ID {}", id);
                
                return product;
            } finally {
                // 6. 락 해제
                redisTemplate.delete(lockKey);
                log.info("락 해제 완료: {}", lockKey);
            }
        } else {
            // 7. 락 획득 실패 시 대기 후 재시도
            log.info("락 획득 실패, 재시도: {}", lockKey);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("대기 중 인터럽트 발생", e);
            }
            return getProductById(id); // 재귀 호출로 재시도
        }
    }
}
