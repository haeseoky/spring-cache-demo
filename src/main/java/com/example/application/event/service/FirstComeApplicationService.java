package com.example.application.event.service;

import com.example.application.event.dto.EventDto;
import com.example.domain.event.entity.Event;
import com.example.domain.event.entity.EventParticipation;
import com.example.domain.event.repository.EventRepository;
import com.example.domain.event.service.FirstComeEventDomainService;
import com.example.infrastructure.lock.DistributedLock;
import com.example.infrastructure.lock.LockException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 선착순 이벤트 애플리케이션 서비스
 */
@Service
public class FirstComeApplicationService {
    
    private static final Logger log = LoggerFactory.getLogger(FirstComeApplicationService.class);
    private static final String EVENT_PARTICIPANT_COUNT_KEY = "event:participants:";
    private static final String LOCK_KEY_PREFIX = "lock:event:";
    private static final Duration LOCK_TIMEOUT = Duration.ofSeconds(10);
    
    private final EventRepository eventRepository;
    private final FirstComeEventDomainService eventDomainService;
    private final DistributedLock spinLock;
    private final DistributedLock luaLock;
    private final DistributedLock redissonLock;
    private final RedisTemplate<String, String> stringRedisTemplate;
    
    // 통계용 카운터
    private final Map<String, AtomicInteger> successCountMap = new HashMap<>();
    private final Map<String, AtomicInteger> failCountMap = new HashMap<>();
    
    public FirstComeApplicationService(
            EventRepository eventRepository,
            FirstComeEventDomainService eventDomainService,
            Map<String, DistributedLock> locks,
            RedisTemplate<String, String> stringRedisTemplate) {
        this.eventRepository = eventRepository;
        this.eventDomainService = eventDomainService;
        this.spinLock = locks.get("redisSpinLock");
        this.luaLock = locks.get("redisLuaLock");
        this.redissonLock = locks.get("redissonLock");
        this.stringRedisTemplate = stringRedisTemplate;
    }
    
    /**
     * 이벤트 초기화
     */
    public EventDto initializeEvent(String eventId, String name, int quantity) {
        // 통계 카운터 초기화
        successCountMap.put(eventId, new AtomicInteger(0));
        failCountMap.put(eventId, new AtomicInteger(0));
        
        // 도메인 서비스를 통해 이벤트 초기화
        return EventDto.fromEntity(eventDomainService.initializeEvent(eventId, name, quantity));
    }
    
    /**
     * 스핀락을 사용한 선착순 이벤트 참여
     */
    public ParticipationResult participateWithSpinLock(String eventId, String userId) {
        log.debug("스핀락을 사용한 이벤트 참여 시도: 이벤트={}, 사용자={}", eventId, userId);
        
        try {
            return participateWithLock(spinLock, eventId, userId);
        } catch (LockException e) {
            log.warn("스핀락 획득 실패: {}", e.getMessage());
            return ParticipationResult.failure("락 획득 실패");
        }
    }
    
    /**
     * Lua 스크립트 락을 사용한 선착순 이벤트 참여
     */
    public ParticipationResult participateWithLuaLock(String eventId, String userId) {
        log.debug("Lua 락을 사용한 이벤트 참여 시도: 이벤트={}, 사용자={}", eventId, userId);
        
        try {
            return participateWithLock(luaLock, eventId, userId);
        } catch (LockException e) {
            log.warn("Lua 락 획득 실패: {}", e.getMessage());
            return ParticipationResult.failure("락 획득 실패");
        }
    }
    
    /**
     * Redisson 락을 사용한 선착순 이벤트 참여
     */
    public ParticipationResult participateWithRedissonLock(String eventId, String userId) {
        log.debug("Redisson 락을 사용한 이벤트 참여 시도: 이벤트={}, 사용자={}", eventId, userId);
        
        try {
            return participateWithLock(redissonLock, eventId, userId);
        } catch (LockException e) {
            log.warn("Redisson 락 획득 실패: {}", e.getMessage());
            return ParticipationResult.failure("락 획득 실패");
        }
    }
    
    /**
     * 특정 락 구현체를 사용한 이벤트 참여 처리
     */
    private ParticipationResult participateWithLock(DistributedLock lock, String eventId, String userId) {
        return lock.executeWithLock(LOCK_KEY_PREFIX + eventId, LOCK_TIMEOUT, () -> {
            // 도메인 서비스를 통해 이벤트 참여 처리
            EventParticipation participation = eventDomainService.participate(eventId, userId);
            
            // 결과 기록
            updateCounters(eventId, participation.isSuccessful());
            
            if (participation.isSuccessful()) {
                return ParticipationResult.success(eventId, userId, eventRepository.getRemainingQuantity(eventId));
            } else {
                return ParticipationResult.failure("선착순 마감");
            }
        });
    }
    
    /**
     * Redis INCR 명령어를 사용한 선착순 이벤트 참여 (락 없음)
     */
    public ParticipationResult participateWithIncr(String eventId, String userId) {
        log.debug("INCR를 사용한 이벤트 참여 시도: 이벤트={}, 사용자={}", eventId, userId);
        
        // 최대 참여 가능 수량
//        int totalQuantity = eventRepository.getRemainingQuantity(eventId);
        int totalQuantity = eventRepository.findById(eventId).orElse(new Event()).getTotalQuantity();
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String participantCountKey = EVENT_PARTICIPANT_COUNT_KEY + eventId;
        
        // INCR로 참여자 수 증가 (원자적 연산)
        Long currentParticipants = stringRedisTemplate.opsForValue().increment(participantCountKey);
        
        // 참여 가능 여부 판단
        if (currentParticipants > totalQuantity) {
            // 이미 마감된 경우 카운트 롤백
//            stringRedisTemplate.opsForValue().decrement(participantCountKey);
            log.debug("참여 실패 (수량 초과): 이벤트={}, 사용자={}, 참여자 수/총수량={}/{}", 
                    eventId, userId, currentParticipants-1, totalQuantity);
            
            // 결과 기록
            updateCounters(eventId, false);
            
            return ParticipationResult.failure("선착순 마감");
        }
        
        // 도메인 엔티티에도 반영
        eventRepository.decreaseQuantity(eventId);
        
        log.debug("참여 성공: 이벤트={}, 사용자={}, 참여자 수/총수량={}/{}", 
                eventId, userId, currentParticipants, totalQuantity);
        
        // 결과 기록
        updateCounters(eventId, true);
        
        return ParticipationResult.success(eventId, userId, totalQuantity - currentParticipants.intValue());
    }
    
    /**
     * 현재 이벤트 상태 조회
     */
    public EventStatusResult getEventStatus(String eventId) {
        int remainingQuantity = eventRepository.getRemainingQuantity(eventId);
        
        EventStatusResult result = new EventStatusResult();
        result.setEventId(eventId);
        result.setRemainingQuantity(remainingQuantity);
        
        if (successCountMap.containsKey(eventId)) {
            result.setSuccessCount(successCountMap.get(eventId).get());
        }
        
        if (failCountMap.containsKey(eventId)) {
            result.setFailCount(failCountMap.get(eventId).get());
        }
        
        return result;
    }
    
    /**
     * 통계 카운터 업데이트
     */
    private void updateCounters(String eventId, boolean success) {
        if (success) {
            // 성공 카운터가 없으면 생성
            successCountMap.computeIfAbsent(eventId, k -> new AtomicInteger(0)).incrementAndGet();
        } else {
            // 실패 카운터가 없으면 생성
            failCountMap.computeIfAbsent(eventId, k -> new AtomicInteger(0)).incrementAndGet();
        }
    }
    
    /**
     * 이벤트 참여 결과
     */
    public static class ParticipationResult {
        private final String eventId;
        private final String userId;
        private final boolean success;
        private final String message;
        private final int remainingQuantity;
        
        private ParticipationResult(String eventId, String userId, boolean success, 
                                  String message, int remainingQuantity) {
            this.eventId = eventId;
            this.userId = userId;
            this.success = success;
            this.message = message;
            this.remainingQuantity = remainingQuantity;
        }
        
        public static ParticipationResult success(String eventId, String userId, int remainingQuantity) {
            return new ParticipationResult(eventId, userId, true, "참여 성공", remainingQuantity);
        }
        
        public static ParticipationResult failure(String message) {
            return new ParticipationResult(null, null, false, message, 0);
        }
        
        // Getters
        public String getEventId() { return eventId; }
        public String getUserId() { return userId; }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public int getRemainingQuantity() { return remainingQuantity; }
    }
    
    /**
     * 이벤트 상태 결과
     */
    public static class EventStatusResult {
        private String eventId;
        private int remainingQuantity;
        private int successCount;
        private int failCount;
        
        // Getters & Setters
        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }
        public int getRemainingQuantity() { return remainingQuantity; }
        public void setRemainingQuantity(int remainingQuantity) { this.remainingQuantity = remainingQuantity; }
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        public int getFailCount() { return failCount; }
        public void setFailCount(int failCount) { this.failCount = failCount; }
    }
}
