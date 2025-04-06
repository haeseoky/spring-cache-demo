package com.example.domain.common;

/**
 * 정렬 방향 열거형
 */
public enum SortDirection {
    ASC, DESC;
    
    public static SortDirection fromString(String value) {
        if (value == null) {
            return DESC;
        }
        
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DESC;
        }
    }
}
