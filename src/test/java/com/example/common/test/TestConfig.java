package com.example.common.test;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 테스트를 위한 구성 클래스입니다.
 * MockSession 어노테이션을 처리하는 AspectJ 설정을 활성화합니다.
 */
@Configuration
@EnableAspectJAutoProxy
@ComponentScan("com.example.common.test")
public class TestConfig {
    // 필요한 경우 추가 구성을 이곳에 추가할 수 있습니다.
}
