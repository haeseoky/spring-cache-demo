package com.example.domain.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    // 활성화된 이벤트만 검색
    List<Event> findByIsActiveTrue();
    
    // 시작일 기준으로 이벤트 검색
    List<Event> findByStartDateAfterOrderByStartDateAsc(LocalDateTime date);
    
    // 종료일 기준으로 이벤트 검색
    List<Event> findByEndDateBeforeOrderByEndDateDesc(LocalDateTime date);
    
    // 현재 등록 가능한 이벤트 검색
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.startDate > CURRENT_TIMESTAMP " +
           "AND (e.maxParticipants IS NULL OR e.currentParticipants < e.maxParticipants)")
    List<Event> findRegistrableEvents();
    
    // 제목이나 설명으로 이벤트 검색
    List<Event> findByTitleContainingOrDescriptionContainingOrderByStartDateAsc(String title, String description);
    
    // 장소별 이벤트 검색
    List<Event> findByLocationContainingOrderByStartDateAsc(String location);
    
    // 주최자별 이벤트 검색
    List<Event> findByOrganizerContainingOrderByStartDateAsc(String organizer);
}
