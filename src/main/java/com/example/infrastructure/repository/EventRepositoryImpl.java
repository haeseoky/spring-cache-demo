package com.example.infrastructure.repository;

import com.example.domain.common.Page;
import com.example.domain.common.Pageable;
import com.example.domain.common.SortDirection;
import com.example.domain.event.entity.Event;
import com.example.domain.event.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

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
        return getFromRedis(key, this::convertToEvent);
    }
    
    /**
     * Redis에서 객체를 조회하고 타입에 맞게 변환하는 공통 메서드
     * @param key Redis 키
     * @param converter 타입 변환기 함수
     * @param <T> 반환 타입
     * @return 조회 결과를 Optional로 래핑
     */
    private <T> Optional<T> getFromRedis(String key, Function<Object, T> converter) {
        Object result = redisTemplate.opsForValue().get(key);
        
        if (result == null) {
            return Optional.empty();
        }
        
        try {
            T converted = converter.apply(result);
            return Optional.ofNullable(converted);
        } catch (Exception e) {
            log.error("Redis 데이터 변환 실패: key={}, resultType={}, error={}", 
                    key, result.getClass().getName(), e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Redis에서 가져온 객체를 Event 타입으로 변환
     */
    private Event convertToEvent(Object result) {
        // 이미 Event 타입인 경우
        if (result instanceof Event) {
            return (Event) result;
        }
        
        // Map 타입인 경우 Event 객체로 변환
        if (result instanceof Map) {
            log.debug("이벤트 조회 결과 변환 필요: 클래스={}", result.getClass().getName());
            return convertMapToEvent((Map<String, Object>) result);
        }
        
        // 알 수 없는 타입
        log.warn("이벤트 조회 결과 타입 오류: {}", result.getClass().getName());
        return null;
    }
    
    /**
     * Map 객체를 Event 객체로 변환
     */
    private Event convertMapToEvent(Map<String, Object> map) {
        Event event = new Event();
        event.setId((String) map.get("id"));
        event.setName((String) map.get("name"));
        event.setDescription((String) map.get("description"));
        
        // 숫자 타입은 Number로 가져와서 int로 변환
        if (map.get("totalQuantity") instanceof Number) {
            event.setTotalQuantity(((Number) map.get("totalQuantity")).intValue());
        }
        
        if (map.get("remainingQuantity") instanceof Number) {
            event.setRemainingQuantity(((Number) map.get("remainingQuantity")).intValue());
        }
        
        // LocalDateTime 처리
        event.setStartTime(convertToLocalDateTime(map.get("startTime")));
        event.setEndTime(convertToLocalDateTime(map.get("endTime")));
        
        // Boolean 타입 변환
        if (map.get("active") instanceof Boolean) {
            event.setActive((Boolean) map.get("active"));
        }
        
        return event;
    }
    
    /**
     * 다양한 형태의 날짜/시간 데이터를 LocalDateTime으로 변환
     */
    private LocalDateTime convertToLocalDateTime(Object timeObject) {
        if (timeObject == null) {
            return null;
        }
        
        try {
            if (timeObject instanceof LocalDateTime) {
                return (LocalDateTime) timeObject;
            } else if (timeObject instanceof String) {
                return LocalDateTime.parse((String) timeObject);
            } else if (timeObject instanceof Map) {
                Map<String, Object> timeMap = (Map<String, Object>) timeObject;
                if (timeMap.containsKey("$jsr310")) {
                    return LocalDateTime.parse((String) timeMap.get("epochSecond"));
                } else if (timeMap.containsKey("year") && timeMap.containsKey("month")) {
                    int year = ((Number) timeMap.get("year")).intValue();
                    int month = ((Number) timeMap.get("month")).intValue();
                    int day = ((Number) timeMap.get("day")).intValue();
                    int hour = ((Number) timeMap.getOrDefault("hour", 0)).intValue();
                    int minute = ((Number) timeMap.getOrDefault("minute", 0)).intValue();
                    int second = ((Number) timeMap.getOrDefault("second", 0)).intValue();
                    int nano = ((Number) timeMap.getOrDefault("nano", 0)).intValue();
                    return LocalDateTime.of(year, month, day, hour, minute, second, nano);
                }
            }
        } catch (Exception e) {
            log.warn("날짜/시간 변환 오류: {}", e.getMessage());
        }
        
        return null;
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
    
    @Override
    public Page<Event> findAll(Pageable pageable) {
        log.debug("이벤트 페이징 조회: {}", pageable);
        
        // Redis에서 EVENT_KEY_PREFIX로 시작하는 모든 키 조회
        ScanOptions options = ScanOptions.scanOptions().match(EVENT_KEY_PREFIX + "*").count(100).build();
        List<String> keys = new ArrayList<>();
        
        redisTemplate.execute((connection) -> {
            try (Cursor<byte[]> cursor = connection.keyCommands().scan(options)) {
                cursor.stream().forEach(key -> {
                    String keyStr = new String(key);
                    keys.add(keyStr);
                });
            }
            return null;
        }, true);
        
        // 모든 이벤트 객체 조회
        List<Event> events = new ArrayList<>();
        for (String key : keys) {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                Event event = convertToEvent(value);
                if (event != null) {
                    events.add(event);
                }
            }
        }
        
        // 정렬 적용
        if ("id".equals(pageable.getSortBy())) {
            events.sort(Comparator.comparing(Event::getId));
        } else if ("name".equals(pageable.getSortBy())) {
            events.sort(Comparator.comparing(Event::getName));
        } else if ("totalQuantity".equals(pageable.getSortBy())) {
            events.sort(Comparator.comparing(Event::getTotalQuantity));
        } else if ("remainingQuantity".equals(pageable.getSortBy())) {
            events.sort(Comparator.comparing(Event::getRemainingQuantity));
        } else if ("startTime".equals(pageable.getSortBy())) {
            events.sort(Comparator.comparing(Event::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())));
        } else if ("endTime".equals(pageable.getSortBy())) {
            events.sort(Comparator.comparing(Event::getEndTime, Comparator.nullsLast(Comparator.naturalOrder())));
        }
        
        // 정렬 방향 적용
        if (SortDirection.DESC.equals(pageable.getDirection())) {
            Collections.reverse(events);
        }
        
        // 페이지네이션 적용
        int start = pageable.getPage() * pageable.getSize();
        int end = Math.min(start + pageable.getSize(), events.size());
        
        if (start >= events.size()) {
            return new Page<>(Collections.emptyList(), pageable.getPage(), pageable.getSize(), events.size());
        }
        
        List<Event> pageContent = events.subList(start, end);
        return new Page<>(pageContent, pageable.getPage(), pageable.getSize(), events.size());
    }
    
    @Override
    public long count() {
        // 모든 이벤트 키 개수 조회
        ScanOptions options = ScanOptions.scanOptions().match(EVENT_KEY_PREFIX + "*").count(100).build();
        List<String> keys = new ArrayList<>();
        
        redisTemplate.execute((connection) -> {
            try (Cursor<byte[]> cursor = connection.keyCommands().scan(options)) {
                cursor.stream().forEach(key -> {
                    String keyStr = new String(key);
                    keys.add(keyStr);
                });
            }
            return null;
        }, true);
        
        return keys.size();
    }
}