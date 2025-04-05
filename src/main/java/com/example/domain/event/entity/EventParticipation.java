package com.example.domain.event.entity;

import java.time.LocalDateTime;

/**
 * 이벤트 참가 기록 엔티티
 */
public class EventParticipation {
    
    private String id;
    private String eventId;
    private String userId;
    private LocalDateTime participatedAt;
    private boolean successful;
    
    // 기본 생성자
    public EventParticipation() {
    }
    
    // 생성자
    public EventParticipation(String id, String eventId, String userId, boolean successful) {
        this.id = id;
        this.eventId = eventId;
        this.userId = userId;
        this.participatedAt = LocalDateTime.now();
        this.successful = successful;
    }
    
    // Getter & Setter
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getEventId() {
        return eventId;
    }
    
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public LocalDateTime getParticipatedAt() {
        return participatedAt;
    }
    
    public void setParticipatedAt(LocalDateTime participatedAt) {
        this.participatedAt = participatedAt;
    }
    
    public boolean isSuccessful() {
        return successful;
    }
    
    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
}
