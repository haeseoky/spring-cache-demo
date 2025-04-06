package com.example.domain.event.repository;

import com.example.domain.common.Page;
import com.example.domain.common.Pageable;
import com.example.domain.event.entity.Event;
import java.util.List;
import java.util.Optional;

/**
 * 이벤트 도메인 리포지토리 인터페이스
 */
public interface EventRepository {
    
    /**
     * 이벤트 ID로 조회
     */
    Optional<Event> findById(String eventId);
    
    /**
     * 이벤트 저장
     */
    Event save(Event event);
    
    /**
     * 이벤트 남은 수량 조회
     */
    int getRemainingQuantity(String eventId);
    
    /**
     * 이벤트 참여 처리 (수량 감소)
     * 
     * @return 성공 여부
     */
    boolean decreaseQuantity(String eventId);
    
    /**
     * 이벤트 초기화 (수량 설정)
     */
    void setQuantity(String eventId, int quantity);
    
    /**
     * 모든 이벤트 페이징 조회
     * 
     * @param pageable 페이징 정보
     * @return 페이징된 이벤트 목록
     */
    Page<Event> findAll(Pageable pageable);
    
    /**
     * 전체 이벤트 개수 조회
     * 
     * @return 전체 이벤트 개수
     */
    long count();
}