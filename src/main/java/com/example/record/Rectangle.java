package com.example.record;

public record Rectangle(double width, double height) implements Shape {
    public Rectangle {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("너비와 높이는 양수여야 합니다");
        }
    }
    
    @Override
    public double area() {
        return width * height;
    }
    
    @Override
    public double perimeter() {
        return 2 * (width + height);
    }
    
    // 정사각형인지 확인
    public boolean isSquare() {
        return Math.abs(width - height) < 0.001; // 부동 소수점 비교
    }
    
    // 대각선 길이 계산
    public double diagonal() {
        return Math.sqrt(width * width + height * height);
    }
}
