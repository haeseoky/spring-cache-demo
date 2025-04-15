package com.example.application.event;

import com.example.domain.event.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventService {
    
    // 이벤트 생성
    Event createEvent(Event event);
    
    // 이벤트 수정
    Event updateEvent(Long id, Event eventDetails);
    
    // 이벤트 삭제
    void deleteEvent(Long id);
    
    // 이벤트 상세 조회
    Optional<Event> getEventById(Long id);
    
    // 모든 이벤트 조회
    List<Event> getAllEvents();
    
    // 활성화된 이벤트만 조회
    List<Event> getActiveEvents();
    
    // 등록 가능한 이벤트 조회
    List<Event> getRegistrableEvents();
    
    // 날짜별 이벤트 조회
    List<Event> getUpcomingEvents(LocalDateTime fromDate);
    
    // 지난 이벤트 조회
    List<Event> getPastEvents(LocalDateTime toDate);
    
    // 검색 기능
    List<Event> searchEvents(String keyword);
    
    // 장소별 이벤트 조회
    List<Event> getEventsByLocation(String location);
    
    // 주최자별 이벤트 조회
    List<Event> getEventsByOrganizer(String organizer);
    
    // 이벤트 참가자 증가
    Event registerParticipant(Long id);
    
    // 이벤트 참가자 감소
    Event cancelRegistration(Long id);
}
