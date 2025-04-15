package com.example.common.test;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * @MockSession 어노테이션을 처리하는 AspectJ 어드바이스입니다.
 * 테스트 메서드 실행 전에 MockHttpSession을 생성하고 설정합니다.
 */
@Aspect
@Component
public class MockSessionAspect extends AbstractTestExecutionListener {

    /**
     * @MockSession 어노테이션이 적용된 테스트 메서드를 가로채서 세션을 설정합니다.
     */
    @Around("@annotation(com.example.common.test.MockSession) || @within(com.example.common.test.MockSession)")
    public Object aroundMockSessionMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        // 메서드와 클래스에서 어노테이션 찾기
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = method.getDeclaringClass();
        
        MockSession methodAnnotation = method.getAnnotation(MockSession.class);
        MockSession classAnnotation = targetClass.getAnnotation(MockSession.class);
        
        // 메서드 어노테이션을 우선하여 사용
        MockSession annotation = methodAnnotation != null ? methodAnnotation : classAnnotation;
        
        if (annotation != null) {
            // 테스트 클래스 객체 가져오기
            Object target = joinPoint.getTarget();
            
            // MockHttpSession 필드 찾기 및 설정
            setupMockSession(target, annotation);
        }
        
        // 원래 메서드 실행
        return joinPoint.proceed();
    }
    
    /**
     * 테스트 클래스에서 MockHttpSession 필드를 찾아 설정합니다.
     */
    private void setupMockSession(Object target, MockSession annotation) {
        try {
            // 'session' 또는 'mockSession' 필드 찾기
            Field sessionField = findSessionField(target.getClass());
            
            if (sessionField != null) {
                sessionField.setAccessible(true);
                
                // 새 MockHttpSession 생성
                MockHttpSession session = new MockHttpSession();
                
                // 세션 ID 설정
                String sessionId = annotation.sessionId();
                if (!StringUtils.hasText(sessionId)) {
                    sessionId = "test-session-" + UUID.randomUUID();
                }
                
                // 기본 속성 설정
                session.setAttribute("userId", annotation.userId());
                session.setAttribute("userName", annotation.userName());
                session.setAttribute("sessionId", sessionId);
                session.setAttribute("creationTime", System.currentTimeMillis());
                
                // 추가 속성 설정
                String[] keys = annotation.attributeKeys();
                String[] values = annotation.attributeValues();
                
                if (keys.length > 0 && keys.length == values.length) {
                    for (int i = 0; i < keys.length; i++) {
                        session.setAttribute(keys[i], values[i]);
                    }
                }
                
                // 세션 필드 설정
                sessionField.set(target, session);
            }
        } catch (Exception e) {
            throw new IllegalStateException("MockHttpSession 설정 중 오류 발생", e);
        }
    }
    
    /**
     * 대상 클래스에서 MockHttpSession 타입의 필드를 찾습니다.
     */
    private Field findSessionField(Class<?> targetClass) {
        // 먼저 'session'이라는 이름의 필드 찾기
        try {
            Field field = targetClass.getDeclaredField("session");
            if (field.getType().equals(MockHttpSession.class)) {
                return field;
            }
        } catch (NoSuchFieldException ignored) {
            // 필드가 없으면 무시
        }
        
        // 'mockSession'이라는 이름의 필드 찾기
        try {
            Field field = targetClass.getDeclaredField("mockSession");
            if (field.getType().equals(MockHttpSession.class)) {
                return field;
            }
        } catch (NoSuchFieldException ignored) {
            // 필드가 없으면 무시
        }
        
        // MockHttpSession 타입의 필드 찾기
        for (Field field : targetClass.getDeclaredFields()) {
            if (field.getType().equals(MockHttpSession.class)) {
                return field;
            }
        }
        
        return null;
    }
    
    /**
     * MockMvc 요청에 세션을 추가하는 유틸리티 메서드입니다.
     */
    public static MockHttpServletRequestBuilder withMockSession(MockHttpServletRequestBuilder builder, 
                                                                MockSession annotation) {
        MockHttpSession session = new MockHttpSession();
        
        // 세션 ID 설정
        String sessionId = annotation.sessionId();
        if (!StringUtils.hasText(sessionId)) {
            sessionId = "test-session-" + UUID.randomUUID();
        }
        
        // 기본 속성 설정
        session.setAttribute("userId", annotation.userId());
        session.setAttribute("userName", annotation.userName());
        session.setAttribute("sessionId", sessionId);
        session.setAttribute("creationTime", System.currentTimeMillis());
        
        // 추가 속성 설정
        String[] keys = annotation.attributeKeys();
        String[] values = annotation.attributeValues();
        
        if (keys.length > 0 && keys.length == values.length) {
            for (int i = 0; i < keys.length; i++) {
                session.setAttribute(keys[i], values[i]);
            }
        }
        
        return builder.session(session);
    }
}
