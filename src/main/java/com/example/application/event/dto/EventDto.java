package com.example.application.event.dto;

import com.example.domain.event.entity.Event;
import java.time.LocalDateTime;

/**
 * 이벤트 DTO
 */
public class EventDto {
    
    private String id;
    private String name;
    private String description;
    private int totalQuantity;
    private int remainingQuantity;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean active;
    
    // 기본 생성자
    public EventDto() {
    }
    
    // 도메인 엔티티로부터 DTO 생성
    public static EventDto fromEntity(Event event) {
        EventDto dto = new EventDto();
        dto.setId(event.getId());
        dto.setName(event.getName());
        dto.setDescription(event.getDescription());
        dto.setTotalQuantity(event.getTotalQuantity());
        dto.setRemainingQuantity(event.getRemainingQuantity());
        dto.setStartTime(event.getStartTime());
        dto.setEndTime(event.getEndTime());
        dto.setActive(event.isActive());
        return dto;
    }
    
    // DTO로부터 도메인 엔티티 생성
    public Event toEntity() {
        Event event = new Event(id, name, description, totalQuantity, startTime, endTime);
        event.setRemainingQuantity(remainingQuantity);
        event.setActive(active);
        return event;
    }
    
    // Getter & Setter
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
