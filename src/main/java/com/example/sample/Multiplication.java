package com.example.sample;

public final class Multiplication extends Operation {
    public Multiplication(double left, double right) {
        super(left, right);
    }
    
    @Override
    public double execute() {
        return getLeft() * getRight();
    }
}
