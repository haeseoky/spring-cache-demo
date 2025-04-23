package com.example.record;

/**
 * 열거형과 함께 사용하는 Record
 * - 타입 안전성 향상
 * - 비즈니스 로직 패턴 매칭
 */
public record Product(String id, String name, Category category, double price) {
    // 카테고리 열거형
    public enum Category {
        ELECTRONICS, CLOTHING, FOOD, BOOKS, OTHER;
        
        // 카테고리별 할인율 반환
        public double getDiscountRate() {
            return switch(this) {
                case ELECTRONICS -> 0.10; // 10% 할인
                case CLOTHING -> 0.20;    // 20% 할인
                case FOOD -> 0.05;        // 5% 할인
                case BOOKS -> 0.15;       // 15% 할인
                case OTHER -> 0.0;        // 할인 없음
            };
        }
    }
    
    // 할인 정책을 적용하는 메서드
    public double getDiscountedPrice() {
        return price * (1 - category.getDiscountRate());
    }
    
    // 세금 포함 가격 계산 (카테고리별 세율 다름)
    public double getPriceWithTax() {
        double taxRate = switch (category) {
            case FOOD -> 0.05;        // 식품: 5% 세금
            case BOOKS -> 0.08;       // 책: 8% 세금
            default -> 0.10;          // 기타: 10% 세금
        };
        
        return price * (1 + taxRate);
    }
}
