package com.example.domain.event.service;

import com.example.domain.event.entity.Event;
import com.example.domain.event.entity.EventParticipation;
import com.example.domain.event.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 선착순 이벤트 도메인 서비스
 * 핵심 비즈니스 로직만 포함
 */
@Service
public class FirstComeEventDomainService {
    
    private static final Logger log = LoggerFactory.getLogger(FirstComeEventDomainService.class);
    
    private final EventRepository eventRepository;
    
    public FirstComeEventDomainService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }
    
    /**
     * 이벤트 참여 처리 - 핵심 비즈니스 로직
     */
    public EventParticipation participate(String eventId, String userId) {
        log.debug("이벤트 참여 도메인 로직 실행: 이벤트={}, 사용자={}", eventId, userId);
        
        // 이벤트 조회
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이벤트입니다: " + eventId));
        
        // 이벤트 참여 가능 여부 체크
        if (!event.isAvailable()) {
            log.debug("이벤트 참여 불가: 이벤트={}, 사용자={}", eventId, userId);
            return createParticipation(eventId, userId, false);
        }
        
        // 수량 감소 시도
        boolean success = event.decreaseQuantity();
        if (success) {
            // 성공 시 이벤트 저장
            eventRepository.save(event);
            log.debug("이벤트 참여 성공: 이벤트={}, 사용자={}, 남은 수량={}", 
                    eventId, userId, event.getRemainingQuantity());
        } else {
            log.debug("이벤트 참여 실패 (수량 부족): 이벤트={}, 사용자={}", eventId, userId);
        }
        
        // 참여 결과 생성
        return createParticipation(eventId, userId, success);
    }
    
    // 참여 결과 생성 헬퍼 메서드
    private EventParticipation createParticipation(String eventId, String userId, boolean success) {
        return new EventParticipation(
                UUID.randomUUID().toString(),
                eventId,
                userId,
                success
        );
    }
    
    /**
     * 이벤트 초기화
     */
    public Event initializeEvent(String eventId, String name, int quantity) {
        log.debug("이벤트 초기화: ID={}, 이름={}, 수량={}", eventId, name, quantity);
        
        Event event = eventRepository.findById(eventId)
                .orElseGet(() -> new Event(
                        eventId,
                        name,
                        "선착순 이벤트",
                        quantity,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(1)
                ));
        
        event.setRemainingQuantity(quantity);
        event.setActive(true);
        
        return eventRepository.save(event);
    }
}
