package com.example.cache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Cache Stampede 방지 기법 테스트 애플리케이션
 */
@SpringBootApplication
@EnableScheduling  // 스케줄링 기능 활성화 (캐시 워밍 용)
public class CacheStampedeApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CacheStampedeApplication.class, args);
    }
}
