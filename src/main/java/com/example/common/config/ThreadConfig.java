package com.example.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 스레드 관련 설정을 담당하는 클래스입니다.
 * Java 21의 Virtual Thread 기능을 활용한 ExecutorService를 제공합니다.
 */
@Configuration
public class ThreadConfig {

    /**
     * Virtual Thread 기반의 ExecutorService를 생성합니다.
     * Virtual Thread는 경량 스레드로, 기존 플랫폼 스레드보다 효율적으로 많은 동시 작업을 처리할 수 있습니다.
     *
     * @return Virtual Thread 기반의 ExecutorService
     */
    @Bean(name = "virtualThreadExecutor")
    public ExecutorService virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
    
    /**
     * 고정 크기의 플랫폼 스레드 풀을 생성합니다.
     * Virtual Thread와의 성능 비교용으로 사용될 수 있습니다.
     *
     * @return 고정 크기의 플랫폼 스레드 풀
     */
    @Bean(name = "platformThreadExecutor")
    public ExecutorService platformThreadExecutor() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }
}
