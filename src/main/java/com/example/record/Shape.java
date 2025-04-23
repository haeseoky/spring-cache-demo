package com.example.record;

/**
 * Record를 이용한 sealed 인터페이스 구현
 * - 패턴 매칭과 함께 사용하는 계층 구조
 */
public sealed interface Shape permits Circle, Rectangle, Triangle {
    double area();
    double perimeter();
}
