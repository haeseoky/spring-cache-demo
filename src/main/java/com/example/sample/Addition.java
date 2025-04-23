package com.example.sample;

public final class Addition extends Operation {
    public Addition(double left, double right) {
        super(left, right);
    }
    
    @Override
    public double execute() {
        return getLeft() + getRight();
    }
}
