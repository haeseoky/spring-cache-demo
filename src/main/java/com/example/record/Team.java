package com.example.record;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * 불변 컬렉션을 사용한 Record
 * - 방어적 복사를 통한 불변성 보장
 * - 추가 접근자 메서드 제공
 */
public record Team(String name, List<String> members) {
    // 방어적 복사를 사용한 생성자
    public Team {
        members = new ArrayList<>(members); // 방어적 복사
        members = Collections.unmodifiableList(members); // 불변 리스트로 변환
    }
    
    // 안전한 접근 메서드 추가
    public List<String> getMembers() {
        return members; // 이미 불변 리스트이므로 안전
    }
    
    // 팀원 수 반환 메서드
    public int size() {
        return members.size();
    }
    
    // 특정 멤버가 포함되어 있는지 확인하는 메서드
    public boolean hasMember(String member) {
        return members.contains(member);
    }
}
