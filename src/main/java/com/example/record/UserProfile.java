package com.example.record;

import java.util.Optional;

/**
 * Optional 필드를 가진 Record
 * - 선택적 필드의 안전한 처리
 * - 다양한 생성자 패턴
 */
public record UserProfile(String userId, String name, Optional<String> bio, Optional<String> website) {
    // 기본값을 제공하는 생성자
    public UserProfile(String userId, String name) {
        this(userId, name, Optional.empty(), Optional.empty());
    }
    
    // 필요한 필드만 제공하는 생성자
    public UserProfile(String userId, String name, String bio) {
        this(userId, name, Optional.ofNullable(bio), Optional.empty());
    }
    
    // 메서드 추가
    public boolean hasCompletedProfile() {
        return bio.isPresent() && website.isPresent();
    }
    
    // 프로필 정보 요약
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" (").append(userId).append(")");
        
        bio.ifPresent(b -> sb.append("\nBio: ").append(b));
        website.ifPresent(w -> sb.append("\nWebsite: ").append(w));
        
        return sb.toString();
    }
}
