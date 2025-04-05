package com.example.application.product.service;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * 캐시 서비스 인터페이스
 */
public interface CacheService<T> {
    
    /**
     * 캐시에서 데이터 조회, 없으면 로드 함수 실행 후 저장
     * 
     * @param key 캐시 키
     * @param loader 데이터 로드 함수
     * @param ttl 캐시 유효 시간
     * @return 데이터
     */
    T getOrLoad(String key, Supplier<T> loader, Duration ttl);
    
    /**
     * 캐시에 데이터 저장
     * 
     * @param key 캐시 키
     * @param value 저장할 데이터
     * @param ttl 캐시 유효 시간
     */
    void put(String key, T value, Duration ttl);
    
    /**
     * 캐시에서 데이터 제거
     * 
     * @param key 캐시 키
     */
    void evict(String key);
    
    /**
     * 캐시에 데이터 존재 여부 확인
     * 
     * @param key 캐시 키
     * @return 존재 여부
     */
    boolean exists(String key);
}
