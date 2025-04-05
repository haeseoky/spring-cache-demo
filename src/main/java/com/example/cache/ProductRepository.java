package com.example.cache;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * 상품 데이터 처리를 위한 Repository
 * 실제 DB 대신 인메모리 Map을 사용하여 구현
 */
@Repository
public class ProductRepository {
    
    private static final Logger log = LoggerFactory.getLogger(ProductRepository.class);
    
    // 인메모리 데이터 저장소
    private final Map<Long, Product> products = new HashMap<>();
    
    // 샘플 데이터 초기화
    public ProductRepository() {
        initSampleData();
    }
    
    private void initSampleData() {
        for (long i = 1; i <= 100; i++) {
            Product product = new Product(
                    i,
                    "Product " + i,
                    "Description for product " + i,
                    BigDecimal.valueOf(10 + i * 0.5),
                    100
            );
            products.put(i, product);
        }
        log.info("샘플 상품 데이터 {} 개 초기화 완료", products.size());
    }
    
    /**
     * ID로 상품 조회 - 의도적으로 지연 추가하여 DB 조회 시뮬레이션
     */
    public Optional<Product> findById(Long id) {
        log.info("DB에서 상품 ID {} 조회 시작", id);
        
        // DB 조회 지연 시뮬레이션
        try {
            Thread.sleep(500);  // 500ms 지연
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Product product = products.get(id);
        log.info("DB에서 상품 ID {} 조회 완료: {}", id, product != null ? "상품 있음" : "상품 없음");
        
        return Optional.ofNullable(product);
    }
    
    /**
     * 인기 상품 ID 목록 반환 (상위 N개)
     */
    public List<Long> findTopProductIds(int limit) {
        log.info("인기 상품 상위 {} 개 조회", limit);
        
        // 실제로는 DB에서 인기 상품을 조회하는 로직
        // 여기서는 간단하게 ID 순서로 반환
        List<Long> topIds = new ArrayList<>();
        int count = Math.min(limit, products.size());
        for (long i = 1; i <= count; i++) {
            topIds.add(i);
        }
        
        return topIds;
    }
    
    /**
     * 상품 저장
     */
    public Product save(Product product) {
        products.put(product.getId(), product);
        return product;
    }
}
