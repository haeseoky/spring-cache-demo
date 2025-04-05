package com.example.infrastructure.lock;

/**
 * 분산락 관련 예외
 */
public class LockException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public LockException(String message) {
        super(message);
    }
    
    public LockException(String message, Throwable cause) {
        super(message, cause);
    }
}
