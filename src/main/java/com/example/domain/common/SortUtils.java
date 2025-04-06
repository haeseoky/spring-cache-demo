package com.example.domain.common;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 정렬 관련 유틸리티 클래스
 * 다양한 도메인 객체에 적용 가능한 정렬 기능 제공
 */
public class SortUtils {

    /**
     * 지정된 속성 및 정렬 방향에 따라 Comparator를 생성
     *
     * @param sortBy 정렬 속성명
     * @param direction 정렬 방향
     * @param propertyExtractors 속성명과 해당 속성 접근자 함수를 매핑한 Map
     * @param <T> 도메인 객체 타입
     * @return 설정된 Comparator
     */
    public static <T> Comparator<T> getComparator(
            String sortBy,
            SortDirection direction,
            Map<String, Function<T, ?>> propertyExtractors) {
        
        // 기본 정렬 속성 이름 (정렬 속성이 없거나 찾을 수 없는 경우 사용)
        String defaultProperty = propertyExtractors.keySet().iterator().next();
        
        // 지정된 속성 접근자 조회 (없으면 기본 속성 사용)
        Function<T, ?> extractor = propertyExtractors.getOrDefault(sortBy, propertyExtractors.get(defaultProperty));
        
        // Comparator 생성 - 동적 타입 처리
        Comparator<T> comparator = (e1, e2) -> {
            Object v1 = extractor.apply(e1);
            Object v2 = extractor.apply(e2);
            
            // null 처리
            if (v1 == null && v2 == null) return 0;
            if (v1 == null) return 1;  // nulls last
            if (v2 == null) return -1; // nulls last
            
            // 안전한 비교를 위한 타입 확인
            if (v1 instanceof Comparable && v1.getClass().isInstance(v2)) {
                @SuppressWarnings("unchecked")
                Comparable<Object> c1 = (Comparable<Object>) v1;
                return c1.compareTo(v2);
            }
            
            // 비교 불가능한 경우 toString() 결과로 비교
            return v1.toString().compareTo(v2.toString());
        };
        
        // 정렬 방향 적용
        return SortDirection.DESC.equals(direction) ? comparator.reversed() : comparator;
    }
}
