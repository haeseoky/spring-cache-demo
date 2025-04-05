package com.example.interfaces.product;

import com.example.application.product.dto.ProductDto;
import com.example.application.product.service.ProductApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 상품 API 컨트롤러
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    
    private final ProductApplicationService productService;
    
    public ProductController(ProductApplicationService productService) {
        this.productService = productService;
    }
    
    /**
     * ID로 상품 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 인기 상품 목록 조회
     */
    @GetMapping("/top")
    public ResponseEntity<List<ProductDto>> getTopProducts(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(productService.getTopProducts(limit));
    }
    
    /**
     * 상품 저장
     */
    @PostMapping
    public ResponseEntity<ProductDto> saveProduct(@RequestBody ProductDto productDto) {
        return ResponseEntity.ok(productService.saveProduct(productDto));
    }
    
    /**
     * 상품 캐시 제거
     */
    @DeleteMapping("/{id}/cache")
    public ResponseEntity<Map<String, Object>> evictProductCache(@PathVariable Long id) {
        productService.evictProductCache(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "상품 캐시가 제거되었습니다");
        response.put("productId", id);
        
        return ResponseEntity.ok(response);
    }
}
