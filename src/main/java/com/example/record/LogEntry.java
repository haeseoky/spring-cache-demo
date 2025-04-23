package com.example.record;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 인터페이스를 구현하는 Record
 * - Serializable, Comparable 인터페이스 구현
 * - 기능 확장 메서드 추가
 */
public record LogEntry(String id, LocalDateTime timestamp, String level, String message) 
        implements Serializable, Comparable<LogEntry> {
    
    // 상수 정의
    public static final String INFO = "INFO";
    public static final String WARNING = "WARNING";
    public static final String ERROR = "ERROR";
    public static final String DEBUG = "DEBUG";
    
    // Comparable 인터페이스 구현
    @Override
    public int compareTo(LogEntry other) {
        return this.timestamp.compareTo(other.timestamp);
    }
    
    // 로그 레벨 확인 메서드
    public boolean isError() {
        return ERROR.equals(level);
    }
    
    public boolean isWarning() {
        return WARNING.equals(level);
    }
    
    public boolean isInfo() {
        return INFO.equals(level);
    }
    
    public boolean isDebug() {
        return DEBUG.equals(level);
    }
    
    // 포맷팅된 문자열 반환
    public String getFormattedLog() {
        return String.format("[%s] [%s] %s - %s",
                timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                level,
                id,
                message);
    }
    
    // 정적 팩토리 메서드
    public static LogEntry info(String message) {
        return new LogEntry(
                java.util.UUID.randomUUID().toString(),
                LocalDateTime.now(),
                INFO,
                message);
    }
    
    public static LogEntry error(String message) {
        return new LogEntry(
                java.util.UUID.randomUUID().toString(),
                LocalDateTime.now(),
                ERROR,
                message);
    }
}
