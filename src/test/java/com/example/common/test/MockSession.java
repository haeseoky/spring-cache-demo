package com.example.common.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 테스트 코드에서 MockHttpSession을 쉽게 설정할 수 있게 해주는 어노테이션입니다.
 * 테스트 클래스나 메서드에 적용하여 세션 속성을 선언적으로 정의할 수 있습니다.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MockSession {
    /**
     * 세션에 설정할 사용자 ID
     */
    String userId() default "testUser";
    
    /**
     * 세션에 설정할 사용자 이름
     */
    String userName() default "Test User";
    
    /**
     * 세션에 설정할 세션 ID
     * 기본값은 비어있으며, 이 경우 자동 생성된 ID가 사용됩니다.
     */
    String sessionId() default "";
    
    /**
     * 세션에 추가로 설정할 속성의 키
     */
    String[] attributeKeys() default {};
    
    /**
     * 세션에 추가로 설정할 속성의 값
     * attributeKeys와 값의 수가 일치해야 합니다.
     */
    String[] attributeValues() default {};
}
