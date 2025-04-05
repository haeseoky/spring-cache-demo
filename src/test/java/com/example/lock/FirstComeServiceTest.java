package com.example.lock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * 선착순 이벤트 서비스 테스트
 */
@SpringBootTest
public class FirstComeServiceTest {

    @Autowired
    private FirstComeService firstComeService;
    
    @Autowired
    private RedisTemplate<String, String> redisLockTemplate;
    
    private static final String TEST_EVENT_ID = "test-event";
    private static final int EVENT_QUANTITY = 50;
    private static final int CONCURRENT_USERS = 100;
    
    @BeforeEach
    void setUp() {
        // 이벤트 초기화
        firstComeService.initializeEvent(TEST_EVENT_ID, EVENT_QUANTITY);
    }
    
    @AfterEach
    void tearDown() {
        // Redis 키 삭제
        redisTemplate.delete("event:count:" + TEST_EVENT_ID);
        redisTemplate.delete("event:participants:" + TEST_EVENT_ID);
        redisTemplate.delete("lock:spin:" + TEST_EVENT_ID);
        redisTemplate.delete("lock:lua:" + TEST_EVENT_ID);
        redisTemplate.delete("lock:redisson:" + TEST_EVENT_ID);
    }
    
    /**
     * 스핀락을 사용한 선착순 테스트
     */
    @Test
    void testParticipateWithSpinLock() throws InterruptedException {
        testConcurrentParticipation("spinlock");
    }
    
    /**
     * Lua 락을 사용한 선착순 테스트
     */
    @Test
    void testParticipateWithLuaLock() throws InterruptedException {
        testConcurrentParticipation("lualock");
    }
    
    /**
     * Redisson 락을 사용한 선착순 테스트
     */
    @Test
    void testParticipateWithRedissonLock() throws InterruptedException {
        testConcurrentParticipation("redisson");
    }
    
    /**
     * Redis INCR 명령어를 사용한 선착순 테스트
     */
    @Test
    void testParticipateWithIncr() throws InterruptedException {
        testConcurrentParticipation("incr");
    }
    
    /**
     * 동시성 테스트 공통 메서드
     */
    private void testConcurrentParticipation(String lockType) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_USERS);
        AtomicInteger successCount = new AtomicInteger(0);
        List<String> userIds = new ArrayList<>();
        
        // 사용자 ID 생성
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            userIds.add("user-" + UUID.randomUUID().toString());
        }
        
        // 동시에 요청 보내기
        for (String userId : userIds) {
            executorService.submit(() -> {
                try {
                    boolean success = false;
                    
                    switch (lockType) {
                        case "spinlock":
                            success = firstComeService.participateWithSpinLock(TEST_EVENT_ID, userId);
                            break;
                        case "lualock":
                            success = firstComeService.participateWithLuaLock(TEST_EVENT_ID, userId);
                            break;
                        case "redisson":
                            success = firstComeService.participateWithRedissonLock(TEST_EVENT_ID, userId);
                            break;
                        case "incr":
                            success = firstComeService.participateWithIncr(TEST_EVENT_ID, userId);
                            break;
                    }
                    
                    if (success) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // 모든 요청이 완료될 때까지 대기
        latch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();
        
        // 최종 결과 확인
        int remainingQuantity = firstComeService.getRemainingQuantity(TEST_EVENT_ID);
        
        System.out.println("Lock Type: " + lockType);
        System.out.println("Success Count: " + successCount.get());
        System.out.println("Remaining Quantity: " + remainingQuantity);
        
        // 성공한 수와 재고 감소량이 일치하는지 확인
        assertEquals(EVENT_QUANTITY, successCount.get());
        assertEquals(0, remainingQuantity);
        
        // 성공한 수가 설정된 수량을 초과하지 않는지 확인
        assertNotEquals(CONCURRENT_USERS, successCount.get());
    }
}
