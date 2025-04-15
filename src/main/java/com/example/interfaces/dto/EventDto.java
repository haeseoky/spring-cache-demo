package com.example.interfaces.dto;

import com.example.domain.event.Event;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class EventDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotBlank(message = "제목은 필수입니다.")
        private String title;
        
        private String description;
        
        @NotNull(message = "시작 날짜는 필수입니다.")
        @Future(message = "시작 날짜는 현재보다 미래여야 합니다.")
        private LocalDateTime startDate;
        
        @NotNull(message = "종료 날짜는 필수입니다.")
        @Future(message = "종료 날짜는 현재보다 미래여야 합니다.")
        private LocalDateTime endDate;
        
        @NotBlank(message = "장소는 필수입니다.")
        private String location;
        
        @NotBlank(message = "주최자는 필수입니다.")
        private String organizer;
        
        @Min(value = 1, message = "최대 참가자 수는 1명 이상이어야 합니다.")
        private Integer maxParticipants;
        
        // Event 도메인 모델로 변환
        public Event toEntity() {
            return Event.builder()
                    .title(title)
                    .description(description)
                    .startDate(startDate)
                    .endDate(endDate)
                    .location(location)
                    .organizer(organizer)
                    .maxParticipants(maxParticipants)
                    .currentParticipants(0)
                    .isActive(true)
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String title;
        private String description;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String location;
        private String organizer;
        private Integer maxParticipants;
        private Integer currentParticipants;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Boolean registrationAvailable;
        
        // Event 도메인 모델에서 DTO로 변환
        public static Response fromEntity(Event event) {
            return Response.builder()
                    .id(event.getId())
                    .title(event.getTitle())
                    .description(event.getDescription())
                    .startDate(event.getStartDate())
                    .endDate(event.getEndDate())
                    .location(event.getLocation())
                    .organizer(event.getOrganizer())
                    .maxParticipants(event.getMaxParticipants())
                    .currentParticipants(event.getCurrentParticipants())
                    .isActive(event.getIsActive())
                    .createdAt(event.getCreatedAt())
                    .updatedAt(event.getUpdatedAt())
                    .registrationAvailable(event.isRegistrationAvailable())
                    .build();
        }
    }
}
