package com.example.domain.event.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 이벤트 도메인 엔티티
 */
public class Event implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String name;
    private String description;
    private int totalQuantity = 0;
    private int remainingQuantity;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean active;
    
    // 기본 생성자
    public Event() {
    }
    
    // 생성자
    public Event(String id, String name, String description, int totalQuantity, 
                LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.totalQuantity = totalQuantity;
        this.remainingQuantity = totalQuantity;
        this.startTime = startTime;
        this.endTime = endTime;
        this.active = true;
    }
    
    // 비즈니스 메서드 - 수량 감소
    public boolean decreaseQuantity() {
        if (remainingQuantity <= 0) {
            return false;
        }
        
        remainingQuantity--;
        return true;
    }
    
    // 비즈니스 메서드 - 이벤트 활성화 상태 체크
    public boolean isAvailable() {
        LocalDateTime now = LocalDateTime.now();
        return active && remainingQuantity > 0 && 
               (startTime == null || !now.isBefore(startTime)) && 
               (endTime == null || !now.isAfter(endTime));
    }
    
    // 게터 및 세터
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getTotalQuantity() {
        return totalQuantity;
    }
    
    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
    
    public int getRemainingQuantity() {
        return remainingQuantity;
    }
    
    public void setRemainingQuantity(int remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
}
