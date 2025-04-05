package com.example.cache;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Stale-While-Revalidate 패턴으로 Cache Stampede 방지
 * 만료된 데이터를 반환하면서 백그라운드에서 새로운 데이터를 갱신
 */
@Service
public class StaleWhileRevalidateService {
    
    private static final Logger log = LoggerFactory.getLogger(StaleWhileRevalidateService.class);
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductRepository productRepository;
    
    public StaleWhileRevalidateService(RedisTemplate<String, Object> redisTemplate, ProductRepository productRepository) {
        this.redisTemplate = redisTemplate;
        this.productRepository = productRepository;
    }
    
    /**
     * Stale-While-Revalidate 패턴으로 상품 조회
     */
    public Product getProductById(Long id) {
        String cacheKey = "swr:fresh:product:" + id;
        String staleCacheKey = "swr:stale:product:" + id;
        
        log.info("Stale-While-Revalidate 방식으로 상품 ID {} 조회 시작", id);
        
        // 1. 신선한 캐시에서 조회
        Product product = (Product) redisTemplate.opsForValue().get(cacheKey);
        
        if (product != null) {
            log.info("신선한 캐시 히트: 상품 ID {}", id);
            return product;
        }
        
        log.info("신선한 캐시 미스: 상품 ID {}", id);
        
        // 2. 오래된 캐시에서 조회
        Product staleProduct = (Product) redisTemplate.opsForValue().get(staleCacheKey);
        
        // 3. 오래된 캐시에서 데이터를 찾은 경우
        if (staleProduct != null) {
            log.info("오래된 캐시 히트, 백그라운드 갱신 시작: 상품 ID {}", id);
            
            // 4. 백그라운드에서 캐시 갱신
            CompletableFuture.runAsync(() -> {
                refreshCache(id, cacheKey, staleCacheKey);
            });
            
            return staleProduct;
        }
        
        log.info("오래된 캐시 미스, 동기적 로드 시작: 상품 ID {}", id);
        
        // 5. 캐시에 아무것도 없는 경우 동기적으로 로드 (첫 로딩)
        return refreshCache(id, cacheKey, staleCacheKey);
    }
    
    /**
     * 캐시 갱신
     */
    private Product refreshCache(Long id, String cacheKey, String staleCacheKey) {
        log.info("DB에서 상품 조회 시작: ID {}", id);
        
        // DB에서 데이터 조회
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        
        // 신선한 캐시에 저장 (TTL: 1분 - 테스트를 위해 짧게 설정)
        redisTemplate.opsForValue().set(cacheKey, product, Duration.ofMinutes(1));
        log.info("신선한 캐시 저장 완료: 상품 ID {}", id);
        
        // 오래된 캐시에 저장 (TTL: 60분)
        redisTemplate.opsForValue().set(staleCacheKey, product, Duration.ofMinutes(60));
        log.info("오래된 캐시 저장 완료: 상품 ID {}", id);
        
        return product;
    }
}
