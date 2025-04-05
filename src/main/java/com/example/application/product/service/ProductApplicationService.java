package com.example.application.product.service;

import com.example.application.product.dto.ProductDto;
import com.example.domain.product.entity.Product;
import com.example.domain.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 상품 애플리케이션 서비스
 */
@Service
public class ProductApplicationService {
    
    private static final Logger log = LoggerFactory.getLogger(ProductApplicationService.class);
    private static final String PRODUCT_CACHE_KEY_PREFIX = "product:";
    private static final Duration PRODUCT_CACHE_TTL = Duration.ofMinutes(10);
    
    private final ProductRepository productRepository;
    private final CacheService<ProductDto> cacheService;
    
    public ProductApplicationService(
            ProductRepository productRepository,
            CacheService<ProductDto> cacheService) {
        this.productRepository = productRepository;
        this.cacheService = cacheService;
    }
    
    /**
     * ID로 상품 조회 (캐시 사용)
     */
    public Optional<ProductDto> getProductById(Long id) {
        String cacheKey = PRODUCT_CACHE_KEY_PREFIX + id;
        
        return Optional.ofNullable(
            cacheService.getOrLoad(cacheKey, () -> 
                productRepository.findById(id)
                    .map(ProductDto::fromEntity)
                    .orElse(null), 
                PRODUCT_CACHE_TTL
            )
        );
    }
    
    /**
     * 인기 상품 목록 조회
     */
    public List<ProductDto> getTopProducts(int limit) {
        List<Long> topProductIds = productRepository.findTopProductIds(limit);
        
        return topProductIds.stream()
                .map(this::getProductById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
    
    /**
     * 상품 저장
     */
    public ProductDto saveProduct(ProductDto productDto) {
        Product savedProduct = productRepository.save(productDto.toEntity());
        ProductDto savedDto = ProductDto.fromEntity(savedProduct);
        
        // 캐시 업데이트
        String cacheKey = PRODUCT_CACHE_KEY_PREFIX + savedProduct.getId();
        cacheService.put(cacheKey, savedDto, PRODUCT_CACHE_TTL);
        
        return savedDto;
    }
    
    /**
     * 상품 캐시 제거
     */
    public void evictProductCache(Long id) {
        String cacheKey = PRODUCT_CACHE_KEY_PREFIX + id;
        cacheService.evict(cacheKey);
        log.debug("상품 캐시 제거: ID={}", id);
    }
}
