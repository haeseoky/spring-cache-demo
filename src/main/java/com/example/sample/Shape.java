package com.example.sample;

// 기본적인 sealed 클래스 예제
public abstract sealed class Shape permits Circle, Rectangle, Triangle {
    private final String name;
    
    public Shape(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public abstract double area();
}
