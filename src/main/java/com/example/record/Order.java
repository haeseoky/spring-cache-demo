package com.example.record;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

/**
 * 중첩된 Record 사용
 * - 연관된 데이터 구조를 내부에 정의
 * - 계층적 데이터 모델링
 */
public record Order(String orderId, Customer customer, List<OrderItem> items) {
    // 중첩 record 정의
    public record Customer(String id, String name, String address) { }
    
    public record OrderItem(String productId, String productName, int quantity, double price) {
        // 계산 메서드 추가
        public double getTotal() {
            return quantity * price;
        }
    }
    
    // 방어적 복사를 사용한 생성자
    public Order {
        items = new ArrayList<>(items);
        items = Collections.unmodifiableList(items);
    }
    
    // 주문 총액 계산 메서드
    public double getTotalAmount() {
        return items.stream()
                .mapToDouble(OrderItem::getTotal)
                .sum();
    }
    
    // 상품 수량 계산 메서드
    public int getTotalQuantity() {
        return items.stream()
                .mapToInt(OrderItem::quantity)
                .sum();
    }
}
