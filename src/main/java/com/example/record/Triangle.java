package com.example.record;

public record Triangle(double side1, double side2, double side3) implements Shape {
    public Triangle {
        if (side1 <= 0 || side2 <= 0 || side3 <= 0) {
            throw new IllegalArgumentException("삼각형의 변의 길이는 양수여야 합니다");
        }
        
        // 삼각형 부등식 검사: 어떤 두 변의 합은 나머지 한 변보다 커야 함
        if (side1 + side2 <= side3 || side1 + side3 <= side2 || side2 + side3 <= side1) {
            throw new IllegalArgumentException("주어진 변의 길이로 삼각형을 만들 수 없습니다");
        }
    }
    
    @Override
    public double area() {
        // 헤론의 공식 사용
        double s = (side1 + side2 + side3) / 2;
        return Math.sqrt(s * (s - side1) * (s - side2) * (s - side3));
    }
    
    @Override
    public double perimeter() {
        return side1 + side2 + side3;
    }
    
    // 삼각형 종류 확인
    public String getType() {
        if (isEquilateral()) {
            return "정삼각형";
        } else if (isIsosceles()) {
            return "이등변삼각형";
        } else {
            return "부등변삼각형";
        }
    }
    
    // 정삼각형 여부
    public boolean isEquilateral() {
        return Math.abs(side1 - side2) < 0.001 && 
               Math.abs(side2 - side3) < 0.001;
    }
    
    // 이등변삼각형 여부
    public boolean isIsosceles() {
        return Math.abs(side1 - side2) < 0.001 || 
               Math.abs(side1 - side3) < 0.001 || 
               Math.abs(side2 - side3) < 0.001;
    }
}
