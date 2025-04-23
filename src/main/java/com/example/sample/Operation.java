package com.example.sample;

// 패턴 매칭과 함께 사용하는 sealed 클래스
public abstract sealed class Operation permits Addition, Subtraction, Multiplication, Division {
    private final double left;
    private final double right;
    
    public Operation(double left, double right) {
        this.left = left;
        this.right = right;
    }
    
    public double getLeft() {
        return left;
    }
    
    public double getRight() {
        return right;
    }
    
    public abstract double execute();
}
