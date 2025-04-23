package com.example.sample;

// 패턴 매칭을 사용한 클라이언트 코드 (Java 17+)
public class Calculator {
    public static double calculate(Operation operation) {
        // Java 17 이상에서 가능한 패턴 매칭
        return switch (operation) {
            case Addition a -> a.getLeft() + a.getRight();
            case Subtraction s -> s.getLeft() - s.getRight();
            case Multiplication m -> m.getLeft() * m.getRight();
            case Division d -> {
                if (d.getRight() == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                yield d.getLeft() / d.getRight();
            }
        };
    }
}
