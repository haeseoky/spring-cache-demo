package com.example.domain.product.repository;

import com.example.domain.product.entity.Product;
import java.util.List;
import java.util.Optional;

/**
 * 상품 도메인 리포지토리 인터페이스
 */
public interface ProductRepository {
    
    /**
     * ID로 상품 조회
     */
    Optional<Product> findById(Long id);
    
    /**
     * 상품 저장
     */
    Product save(Product product);
    
    /**
     * 인기 상품 ID 목록 조회
     */
    List<Long> findTopProductIds(int limit);
}
