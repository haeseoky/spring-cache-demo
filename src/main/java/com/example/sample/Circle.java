package com.example.sample;

public final class Circle extends Shape {
    private final double radius;
    
    public Circle(String name, double radius) {
        super(name);
        this.radius = radius;
    }
    
    @Override
    public double area() {
        return Math.PI * radius * radius;
    }
    
    public double getRadius() {
        return radius;
    }
}
