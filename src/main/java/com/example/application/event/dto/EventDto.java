package com.example.application.event.dto;

import com.example.domain.event.entity.Event;

import java.time.LocalDateTime;

/**
 * 이벤트 DTO 클래스
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

    // 생성자
    public EventDto(String id, String name, String description, int totalQuantity, int remainingQuantity,
                    LocalDateTime startTime, LocalDateTime endTime, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.totalQuantity = totalQuantity;
        this.remainingQuantity = remainingQuantity;
        this.startTime = startTime;
        this.endTime = endTime;
        this.active = active;
    }

    // Entity -> DTO 변환 메서드
    public static EventDto fromEntity(Event event) {
        if (event == null) {
            return null;
        }

        return new EventDto(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getTotalQuantity(),
                event.getRemainingQuantity(),
                event.getStartTime(),
                event.getEndTime(),
                event.isActive()
        );
    }

    // DTO -> Entity 변환 메서드
    public Event toEntity() {
        Event event = new Event();
        event.setId(this.id);
        event.setName(this.name);
        event.setDescription(this.description);
        event.setTotalQuantity(this.totalQuantity);
        event.setRemainingQuantity(this.remainingQuantity);
        event.setStartTime(this.startTime);
        event.setEndTime(this.endTime);
        event.setActive(this.active);
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
