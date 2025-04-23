package com.example.record;

/**
 * 정적 팩토리 메서드를 사용한 Record
 * - 인스턴스 생성을 위한 다양한 방법 제공
 * - 추가 메서드 구현
 */
public record Point(double x, double y) {
    // 정적 팩토리 메서드
    public static Point origin() {
        return new Point(0, 0);
    }
    
    public static Point fromPolar(double radius, double angle) {
        return new Point(radius * Math.cos(angle), radius * Math.sin(angle));
    }
    
    // 메서드 추가
    public double distanceFromOrigin() {
        return Math.sqrt(x * x + y * y);
    }
    
    public double distanceTo(Point other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
