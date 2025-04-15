package com.example.domain.event;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String organizer;

    @Column
    private Integer maxParticipants;

    @Column
    private Integer currentParticipants;

    @Column
    private Boolean isActive;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    // 비즈니스 로직

    public boolean isRegistrationAvailable() {
        return isActive && 
               LocalDateTime.now().isBefore(startDate) && 
               (maxParticipants == null || currentParticipants < maxParticipants);
    }

    public void incrementParticipants() {
        if (this.currentParticipants == null) {
            this.currentParticipants = 0;
        }
        this.currentParticipants++;
    }

    public void decrementParticipants() {
        if (this.currentParticipants != null && this.currentParticipants > 0) {
            this.currentParticipants--;
        }
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.currentParticipants == null) {
            this.currentParticipants = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
