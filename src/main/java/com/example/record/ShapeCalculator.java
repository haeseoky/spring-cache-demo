package com.example.record;

/**
 * 패턴 매칭을 사용한 Record 처리
 * - Java 17+ switch 표현식 활용
 * - 타입별 특화 처리
 */
public class ShapeCalculator {
    // 패턴 매칭을 사용한 면적 계산 (Java 17+)
    public static double calculateArea(Shape shape) {
        return switch (shape) {
            case Circle c -> Math.PI * c.radius() * c.radius();
            case Rectangle r -> r.width() * r.height();
            case Triangle t -> {
                double s = (t.side1() + t.side2() + t.side3()) / 2;
                yield Math.sqrt(s * (s - t.side1()) * (s - t.side2()) * (s - t.side3()));
            }
        };
    }
    
    // 패턴 매칭을 사용한 둘레 계산 (Java 17+)
    public static double calculatePerimeter(Shape shape) {
        return switch (shape) {
            case Circle c -> 2 * Math.PI * c.radius();
            case Rectangle r -> 2 * (r.width() + r.height());
            case Triangle t -> t.side1() + t.side2() + t.side3();
        };
    }
    
    // 도형의 설명 생성
    public static String describeShape(Shape shape) {
        return switch (shape) {
            case Circle c -> String.format("원 (반지름: %.2f)", c.radius());
            case Rectangle r -> {
                if (r.isSquare()) {
                    yield String.format("정사각형 (변의 길이: %.2f)", r.width());
                } else {
                    yield String.format("직사각형 (너비: %.2f, 높이: %.2f)", r.width(), r.height());
                }
            }
            case Triangle t -> String.format("%s (변의 길이: %.2f, %.2f, %.2f)",
                    t.getType(), t.side1(), t.side2(), t.side3());
        };
    }
}
