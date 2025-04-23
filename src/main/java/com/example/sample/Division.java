package com.example.sample;

public final class Division extends Operation {
    public Division(double left, double right) {
        super(left, right);
    }
    
    @Override
    public double execute() {
        if (getRight() == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return getLeft() / getRight();
    }
}
