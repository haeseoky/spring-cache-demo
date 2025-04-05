package com.example.infrastructure.lock;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * 분산 환경에서 사용할 수 있는 락 인터페이스
 */
public interface DistributedLock {
    
    /**
     * 락을 획득합니다.
     * 
     * @param key 락 식별자
     * @param timeout 락 획득 시도 제한 시간
     * @return 락 획득 성공 여부
     */
    boolean acquire(String key, Duration timeout);
    
    /**
     * 락을 해제합니다.
     * 
     * @param key 락 식별자
     * @return 락 해제 성공 여부
     */
    boolean release(String key);
    
    /**
     * 락을 획득하고 실행 후 락을 해제하는 템플릿 메서드
     * 
     * @param <T> 반환 타입
     * @param key 락 식별자
     * @param timeout 락 획득 시도 제한 시간
     * @param supplier 락 획득 후 실행할 로직
     * @return 실행 결과
     * @throws LockException 락 획득 실패 시 발생
     */
    default <T> T executeWithLock(String key, Duration timeout, Supplier<T> supplier) throws LockException {
        boolean acquired = acquire(key, timeout);
        if (!acquired) {
            throw new LockException("분산락 획득 실패: " + key);
        }
        
        try {
            return supplier.get();
        } finally {
            release(key);
        }
    }
    
    /**
     * 락을 획득하고 실행 후 락을 해제하는 템플릿 메서드 (반환값 없음)
     * 
     * @param key 락 식별자
     * @param timeout 락 획득 시도 제한 시간
     * @param runnable 락 획득 후 실행할 로직
     * @throws LockException 락 획득 실패 시 발생
     */
    default void executeWithLock(String key, Duration timeout, Runnable runnable) throws LockException {
        executeWithLock(key, timeout, () -> {
            runnable.run();
            return null;
        });
    }
}
