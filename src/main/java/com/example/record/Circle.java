package com.example.record;

public record Circle(double radius) implements Shape {
    public Circle {
        if (radius <= 0) {
            throw new IllegalArgumentException("반지름은 양수여야 합니다");
        }
    }

    @Override
    public double area() {
        return Math.PI * radius * radius;
    }
    
    @Override
    public double perimeter() {
        return 2 * Math.PI * radius;
    }
    
    // 원의 지름 계산
    public double diameter() {
        return 2 * radius;
    }
}
