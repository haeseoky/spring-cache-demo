package com.example.infrastructure.repository;

import com.example.domain.event.entity.Event;
import com.example.domain.event.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

/**
 * 이벤트 리포지토리 Redis 구현체
 */
@Repository
public class EventRepositoryImpl implements EventRepository {
    
    private static final Logger log = LoggerFactory.getLogger(EventRepositoryImpl.class);
    private static final String EVENT_KEY_PREFIX = "event:";
    private static final String EVENT_COUNT_KEY_PREFIX = "event:count:";
    private static final Duration EVENT_TTL = Duration.ofDays(1);
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTemplate<String, String> stringRedisTemplate;
    
    public EventRepositoryImpl(
            RedisTemplate<String, Object> redisTemplate,
            RedisTemplate<String, String> stringRedisTemplate) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }
    
    @Override
    public Optional<Event> findById(String eventId) {
        log.debug("이벤트 조회: ID={}", eventId);
        String key = EVENT_KEY_PREFIX + eventId;
        
        Event event = (Event) redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(event);
    }
    
    @Override
    public Event save(Event event) {
        log.debug("이벤트 저장: ID={}, 이름={}, 남은 수량={}", 
                event.getId(), event.getName(), event.getRemainingQuantity());
        
        String key = EVENT_KEY_PREFIX + event.getId();
        redisTemplate.opsForValue().set(key, event, EVENT_TTL);
        
        // 수량 정보도 별도 카운터로 저장
        String countKey = EVENT_COUNT_KEY_PREFIX + event.getId();
        stringRedisTemplate.opsForValue().set(countKey, 
                String.valueOf(event.getRemainingQuantity()), EVENT_TTL);
        
        return event;
    }
    
    @Override
    public int getRemainingQuantity(String eventId) {
        String countKey = EVENT_COUNT_KEY_PREFIX + eventId;
        String countStr = stringRedisTemplate.opsForValue().get(countKey);
        
        if (countStr == null) {
            // 카운터가 없으면 이벤트 엔티티에서 조회
            return findById(eventId)
                    .map(Event::getRemainingQuantity)
                    .orElse(0);
        }
        
        return Integer.parseInt(countStr);
    }
    
    @Override
    public boolean decreaseQuantity(String eventId) {
        String countKey = EVENT_COUNT_KEY_PREFIX + eventId;
        
        // 현재 수량 조회
        String countStr = stringRedisTemplate.opsForValue().get(countKey);
        if (countStr == null) {
            return false;
        }
        
        int count = Integer.parseInt(countStr);
        if (count <= 0) {
            return false;
        }
        
        // 수량 감소
        Long newCount = stringRedisTemplate.opsForValue().decrement(countKey);
        log.debug("이벤트 수량 감소: ID={}, 이전={}, 이후={}", eventId, count, newCount);
        
        // 이벤트 엔티티도 업데이트
        findById(eventId).ifPresent(event -> {
            event.setRemainingQuantity(newCount.intValue());
            redisTemplate.opsForValue().set(EVENT_KEY_PREFIX + eventId, event, EVENT_TTL);
        });
        
        return true;
    }
    
    @Override
    public void setQuantity(String eventId, int quantity) {
        String countKey = EVENT_COUNT_KEY_PREFIX + eventId;
        
        // 수량 설정
        stringRedisTemplate.opsForValue().set(countKey, String.valueOf(quantity), EVENT_TTL);
        log.debug("이벤트 수량 설정: ID={}, 수량={}", eventId, quantity);
        
        // 이벤트 엔티티도 업데이트
        findById(eventId).ifPresent(event -> {
            event.setRemainingQuantity(quantity);
            redisTemplate.opsForValue().set(EVENT_KEY_PREFIX + eventId, event, EVENT_TTL);
        });
    }
}
