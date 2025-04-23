package com.example.record;

/**
 * 제네릭 Record
 * - 타입 파라미터를 사용한 유연한 데이터 구조
 * - 다양한 타입에 대한 재사용성 
 */
public record Pair<K, V>(K key, V value) {
    // 두 값 교체하기
    public Pair<V, K> swap() {
        return new Pair<>(value, key);
    }
    
    // 값 변경 메서드 (불변객체이므로 새 객체 생성)
    public Pair<K, V> withKey(K newKey) {
        return new Pair<>(newKey, value);
    }
    
    public Pair<K, V> withValue(V newValue) {
        return new Pair<>(key, newValue);
    }
    
    // 정적 팩토리 메서드
    public static <K, V> Pair<K, V> of(K key, V value) {
        return new Pair<>(key, value);
    }
    
    // 기본값과 함께 생성하는 메서드
    public static <T> Pair<T, T> empty(T defaultValue) {
        return new Pair<>(defaultValue, defaultValue);
    }
    
    // Map에 저장하기 전에 필요한 메서드
    @Override
    public int hashCode() {
        return key == null ? 0 : key.hashCode();
    }
}
