package com.example.sample;

public final class Subtraction extends Operation {
    public Subtraction(double left, double right) {
        super(left, right);
    }
    
    @Override
    public double execute() {
        return getLeft() - getRight();
    }
}
