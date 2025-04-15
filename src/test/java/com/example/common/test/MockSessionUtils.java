package com.example.common.test;

import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * MockSession 어노테이션과 MockHttpSession을 다루기 위한 유틸리티 클래스입니다.
 */
public class MockSessionUtils {
    
    /**
     * MockHttpSession 객체를 생성하고 기본값을 설정합니다.
     * 
     * @param userId 사용자 ID
     * @param userName 사용자 이름
     * @return 설정된 MockHttpSession 객체
     */
    public static MockHttpSession createMockSession(String userId, String userName) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", userId);
        session.setAttribute("userName", userName);
        session.setAttribute("creationTime", System.currentTimeMillis());
        return session;
    }
    
    /**
     * MockHttpSession 객체를 생성하고 맵에서 속성을 설정합니다.
     * 
     * @param attributes 세션에 설정할 속성들
     * @return 설정된 MockHttpSession 객체
     */
    public static MockHttpSession createMockSession(Map<String, Object> attributes) {
        MockHttpSession session = new MockHttpSession();
        attributes.forEach(session::setAttribute);
        return session;
    }
    
    /**
     * MockHttpServletRequestBuilder에 MockHttpSession을 추가합니다.
     * 
     * @param builder MockHttpServletRequestBuilder 객체
     * @param userId 사용자 ID
     * @param userName 사용자 이름
     * @return 업데이트된 MockHttpServletRequestBuilder 객체
     */
    public static MockHttpServletRequestBuilder withSession(
            MockHttpServletRequestBuilder builder, 
            String userId, 
            String userName) {
        
        return builder.session(createMockSession(userId, userName));
    }
    
    /**
     * MockHttpServletRequestBuilder에 속성이 설정된 MockHttpSession을 추가합니다.
     * 
     * @param builder MockHttpServletRequestBuilder 객체
     * @param attributes 세션에 설정할 속성들
     * @return 업데이트된 MockHttpServletRequestBuilder 객체
     */
    public static MockHttpServletRequestBuilder withSession(
            MockHttpServletRequestBuilder builder, 
            Map<String, Object> attributes) {
        
        return builder.session(createMockSession(attributes));
    }
    
    /**
     * Builder 패턴을 사용하여 MockHttpSession을 생성합니다.
     * 
     * @return MockSessionBuilder 객체
     */
    public static MockSessionBuilder builder() {
        return new MockSessionBuilder();
    }
    
    /**
     * MockHttpSession을 생성하기 위한 Builder 클래스입니다.
     */
    public static class MockSessionBuilder {
        private final Map<String, Object> attributes = new HashMap<>();
        
        /**
         * 사용자 ID를 설정합니다.
         * 
         * @param userId 사용자 ID
         * @return 이 빌더
         */
        public MockSessionBuilder userId(String userId) {
            this.attributes.put("userId", userId);
            return this;
        }
        
        /**
         * 사용자 이름을 설정합니다.
         * 
         * @param userName 사용자 이름
         * @return 이 빌더
         */
        public MockSessionBuilder userName(String userName) {
            this.attributes.put("userName", userName);
            return this;
        }
        
        /**
         * 세션 ID를 설정합니다.
         * 
         * @param sessionId 세션 ID
         * @return 이 빌더
         */
        public MockSessionBuilder sessionId(String sessionId) {
            this.attributes.put("sessionId", sessionId);
            return this;
        }
        
        /**
         * 세션에 속성을 추가합니다.
         * 
         * @param key 속성 키
         * @param value 속성 값
         * @return 이 빌더
         */
        public MockSessionBuilder attribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }
        
        /**
         * 세션 생성 시간을 설정합니다.
         * 
         * @param creationTime 생성 시간(밀리초)
         * @return 이 빌더
         */
        public MockSessionBuilder creationTime(long creationTime) {
            this.attributes.put("creationTime", creationTime);
            return this;
        }
        
        /**
         * MockHttpSession을 생성합니다.
         * 
         * @return 구성된 MockHttpSession 객체
         */
        public MockHttpSession build() {
            if (!attributes.containsKey("creationTime")) {
                attributes.put("creationTime", System.currentTimeMillis());
            }
            return createMockSession(attributes);
        }
        
        /**
         * MockHttpServletRequestBuilder에 구성된 MockHttpSession을 추가합니다.
         * 
         * @param builder MockHttpServletRequestBuilder 객체
         * @return 업데이트된 MockHttpServletRequestBuilder 객체
         */
        public MockHttpServletRequestBuilder applyTo(MockHttpServletRequestBuilder builder) {
            return builder.session(build());
        }
    }
}
