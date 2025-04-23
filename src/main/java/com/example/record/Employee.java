package com.example.record;

/**
 * 커스텀 생성자를 가진 Record
 * - 유효성 검사 로직 추가
 * - 생성자 오버로딩
 */
public record Employee(String id, String name, double salary) {
    // 컴팩트 생성자 (추가 유효성 검사)
    public Employee {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID는 필수입니다");
        }
        if (salary < 0) {
            throw new IllegalArgumentException("급여는 음수일 수 없습니다");
        }
    }
    
    // 추가 생성자 오버로딩
    public Employee(String id, String name) {
        this(id, name, 0.0); // 기본 생성자 위임
    }
}
