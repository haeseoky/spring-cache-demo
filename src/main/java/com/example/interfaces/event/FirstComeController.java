package com.example.interfaces.event;

import com.example.application.event.dto.EventDto;
import com.example.application.event.service.FirstComeApplicationService;
import com.example.application.event.service.FirstComeApplicationService.ParticipationResult;
import com.example.application.event.service.FirstComeApplicationService.EventStatusResult;
import com.example.common.dto.PageResponse;
import com.example.domain.common.SortDirection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 선착순 이벤트 API 컨트롤러
 */
@RestController
@RequestMapping("/api/events/first-come")
public class FirstComeController {
    
    private static final Logger log = LoggerFactory.getLogger(FirstComeController.class);
    
    private final FirstComeApplicationService eventService;
    
    public FirstComeController(FirstComeApplicationService eventService) {
        this.eventService = eventService;
    }
    
    /**
     * 이벤트 초기화
     */
    @PostMapping("/init/{eventId}")
    public ResponseEntity<Map<String, Object>> initializeEvent(
            @PathVariable String eventId,
            @RequestParam(defaultValue = "선착순 이벤트") String name,
            @RequestParam(defaultValue = "100") int quantity) {
        
        EventDto event = eventService.initializeEvent(eventId, name, quantity);
        
        Map<String, Object> response = new HashMap<>();
        response.put("eventId", event.getId());
        response.put("name", event.getName());
        response.put("quantity", event.getTotalQuantity());
        response.put("status", "initialized");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 스핀락을 사용한 선착순 이벤트 참여
     */
    @PostMapping("/participate/spinlock/{eventId}")
    public ResponseEntity<Map<String, Object>> participateWithSpinLock(
            @PathVariable String eventId,
            @RequestParam(required = false) String userId) {
        
        if (userId == null) {
            userId = "user-" + UUID.randomUUID().toString();
        }
        
        ParticipationResult result = eventService.participateWithSpinLock(eventId, userId);
        return createResponse(result, "spinlock");
    }
    
    /**
     * Lua 스크립트 락을 사용한 선착순 이벤트 참여
     */
    @PostMapping("/participate/lualock/{eventId}")
    public ResponseEntity<Map<String, Object>> participateWithLuaLock(
            @PathVariable String eventId,
            @RequestParam(required = false) String userId) {
        
        if (userId == null) {
            userId = "user-" + UUID.randomUUID().toString();
        }
        
        ParticipationResult result = eventService.participateWithLuaLock(eventId, userId);
        return createResponse(result, "lualock");
    }
    
    /**
     * Redisson 락을 사용한 선착순 이벤트 참여
     */
    @PostMapping("/participate/redisson/{eventId}")
    public ResponseEntity<Map<String, Object>> participateWithRedissonLock(
            @PathVariable String eventId,
            @RequestParam(required = false) String userId) {
        
        if (userId == null) {
            userId = "user-" + UUID.randomUUID().toString();
        }
        
        ParticipationResult result = eventService.participateWithRedissonLock(eventId, userId);
        return createResponse(result, "redisson");
    }
    
    /**
     * Redis INCR 명령어를 사용한 선착순 이벤트 참여
     */
    @PostMapping("/participate/incr/{eventId}")
    public ResponseEntity<Map<String, Object>> participateWithIncr(
            @PathVariable String eventId,
            @RequestParam(required = false) String userId) {
        
        if (userId == null) {
            userId = "user-" + UUID.randomUUID().toString();
        }
        
        ParticipationResult result = eventService.participateWithIncr(eventId, userId);
        return createResponse(result, "incr");
    }
    
    /**
     * 테스트용 - 동시에 다수의 요청 시뮬레이션
     */
    @GetMapping("/simulate/{eventId}")
    public ResponseEntity<Map<String, Object>> simulateConcurrentRequests(
            @PathVariable String eventId,
            @RequestParam(defaultValue = "100") int concurrentUsers,
            @RequestParam(defaultValue = "spinlock") String lockType) {
        
        log.info("동시 요청 시뮬레이션 시작: 이벤트={}, 동시 요청={}, 락 타입={}", 
                eventId, concurrentUsers, lockType);
        
        // 스레드 풀 생성
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        
        // 동시 요청 실행
        CompletableFuture<?>[] futures = new CompletableFuture[concurrentUsers];
        
        for (int i = 0; i < concurrentUsers; i++) {
            final int userId = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    String userIdStr = "user-" + userId;
                    
                    switch (lockType) {
                        case "spinlock":
                            eventService.participateWithSpinLock(eventId, userIdStr);
                            break;
                        case "lualock":
                            eventService.participateWithLuaLock(eventId, userIdStr);
                            break;
                        case "redisson":
                            eventService.participateWithRedissonLock(eventId, userIdStr);
                            break;
                        case "incr":
                            eventService.participateWithIncr(eventId, userIdStr);
                            break;
                        default:
                            log.warn("알 수 없는 락 타입: {}", lockType);
                    }
                } catch (Exception e) {
                    log.error("요청 처리 중 오류 발생: {}", e.getMessage(), e);
                }
            }, executorService);
        }
        
        // 모든 요청이 완료될 때까지 대기
        CompletableFuture.allOf(futures).join();
        
        // 스레드 풀 종료
        executorService.shutdown();
        
        // 결과 조회
        EventStatusResult status = eventService.getEventStatus(eventId);
        
        // 결과 생성
        Map<String, Object> response = new HashMap<>();
        response.put("eventId", eventId);
        response.put("concurrentUsers", concurrentUsers);
        response.put("lockType", lockType);
        response.put("successCount", status.getSuccessCount());
        response.put("failCount", status.getFailCount());
        response.put("remainingQuantity", status.getRemainingQuantity());
        
        log.info("동시 요청 시뮬레이션 완료: {}", response);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 이벤트 현황 조회
     */
    @GetMapping("/status/{eventId}")
    public ResponseEntity<EventStatusResult> getEventStatus(@PathVariable String eventId) {
        return ResponseEntity.ok(eventService.getEventStatus(eventId));
    }
    
    /**
     * 페이지네이션을 사용한 모든 이벤트 조회
     */
    @GetMapping
    public ResponseEntity<PageResponse<EventDto>> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        // 애플리케이션 서비스 호출 - 기본 파라미터 전달
        PageResponse<EventDto> response = eventService.getAllEventsWithPagination(page, size, sortBy, direction);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 응답 생성
     */
    private ResponseEntity<Map<String, Object>> createResponse(ParticipationResult result, String lockType) {
        Map<String, Object> response = new HashMap<>();
        
        if (result.isSuccess()) {
            response.put("eventId", result.getEventId());
            response.put("userId", result.getUserId());
            response.put("success", true);
            response.put("message", result.getMessage());
            response.put("remainingQuantity", result.getRemainingQuantity());
        } else {
            response.put("success", false);
            response.put("message", result.getMessage());
        }
        
        response.put("lockType", lockType);
        
        return ResponseEntity.ok(response);
    }
}