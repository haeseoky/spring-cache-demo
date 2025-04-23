package com.example.record;

/**
 * 기본적인 record 정의
 * - 불변 데이터 객체로 자동 구현됨
 * - 모든 필드는 private final로 생성됨
 * - 생성자, getter, equals(), hashCode(), toString() 자동 생성
 */
public record Person(String name, int age, String email) {
    // 자동으로 생성됨:
    // - 생성자
    // - getter 메서드 (name(), age(), email())
    // - equals(), hashCode(), toString() 메서드
}
