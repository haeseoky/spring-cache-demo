package com.example.lock;

import com.example.application.event.service.FirstComeApplicationService;
import com.example.application.event.service.FirstComeApplicationService.ParticipationResult;
import java.util.concurrent.Phaser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 선착순 이벤트 서비스 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
public class FirstComeServiceTest {

    private static final Logger log = LoggerFactory.getLogger(FirstComeServiceTest.class);

    @Autowired
    private FirstComeApplicationService firstComeService;
    
    @Autowired
    @Qualifier("lockStringRedisTemplate")
    private RedisTemplate<String, String> lockStringRedisTemplate;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String TEST_EVENT_ID = "test-event";
    private static final String TEST_EVENT_NAME = "테스트 이벤트";
    private static final int EVENT_QUANTITY = 50;
    private static final int CONCURRENT_USERS = 200;
    
    @BeforeEach
    void setUp() {
        // 이벤트 초기화
        log.info("테스트 이벤트 초기화: ID={}, 수량={}", TEST_EVENT_ID, EVENT_QUANTITY);
        firstComeService.initializeEvent(TEST_EVENT_ID, TEST_EVENT_NAME, EVENT_QUANTITY);
    }
    
    @AfterEach
    void tearDown() {
        // Redis 키 삭제
        log.info("Redis 키 정리 중...");
        redisTemplate.delete("event:" + TEST_EVENT_ID);
        redisTemplate.delete("event:count:" + TEST_EVENT_ID);
        redisTemplate.delete("event:participants:" + TEST_EVENT_ID);
        lockStringRedisTemplate.delete("lock:spin:" + TEST_EVENT_ID);
        lockStringRedisTemplate.delete("lock:lua:" + TEST_EVENT_ID);
        lockStringRedisTemplate.delete("lock:redisson:" + TEST_EVENT_ID);
    }
    
    /**
     * 스핀락을 사용한 선착순 테스트
     */
    @Test
    void testParticipateWithSpinLock() throws InterruptedException {
        log.info("스핀락 테스트 시작");
        testConcurrentParticipation("spinlock");

    }
    
    /**
     * Lua 락을 사용한 선착순 테스트
     */
    @Test
    void testParticipateWithLuaLock() throws InterruptedException {
        log.info("Lua 락 테스트 시작");
        testConcurrentParticipation("lualock");
    }
    
    /**
     * Redisson 락을 사용한 선착순 테스트
     */
    @Test
    void testParticipateWithRedissonLock() throws InterruptedException {
        log.info("Redisson 락 테스트 시작");
        testConcurrentParticipation("redisson");
    }
    
    /**
     * Redis INCR 명령어를 사용한 선착순 테스트
     */
    @Test
    void testParticipateWithIncr() throws InterruptedException {
        log.info("INCR 테스트 시작");
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
        
        log.info("테스트 시작: [{}] - 동시 사용자 {} 명, 재고 {} 개", lockType, CONCURRENT_USERS, EVENT_QUANTITY);
        
        // 동시에 요청 보내기
        for (String userId : userIds) {
            executorService.submit(() -> {
                try {
                    ParticipationResult result = null;
                    
                    switch (lockType) {
                        case "spinlock":
                            result = firstComeService.participateWithSpinLock(TEST_EVENT_ID, userId);
                            break;
                        case "lualock":
                            result = firstComeService.participateWithLuaLock(TEST_EVENT_ID, userId);
                            break;
                        case "redisson":
                            result = firstComeService.participateWithRedissonLock(TEST_EVENT_ID, userId);
                            break;
                        case "incr":
                            result = firstComeService.participateWithIncr(TEST_EVENT_ID, userId);
                            break;
                        default:
                            log.warn("지원하지 않는 락 타입: {}", lockType);
                    }
                    
                    if (result != null && result.isSuccess()) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // 모든 요청이 완료될 때까지 대기
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();
        
        assertTrue(completed, "모든 요청이 시간 내에 완료되지 않았습니다.");
        
        // 최종 결과 확인
        var status = firstComeService.getEventStatus(TEST_EVENT_ID);
        int remainingQuantity = status.getRemainingQuantity();
        
        log.info("테스트 결과: [{}] - 성공 {} 건, 남은 수량 {} 개", 
                lockType, successCount.get(), remainingQuantity);
        
        // 성공한 수와 재고 감소량이 일치하는지 확인
        assertEquals(EVENT_QUANTITY, successCount.get(), 
                "성공한 건수가 재고 수량과 일치하지 않습니다.");
        assertEquals(0, remainingQuantity, 
                "남은 재고가 0이 아닙니다.");

        dupTest(lockType);
    }



    /**
     * 스핀락을 사용한 선착순 테스트
     */
    @Test
    void testParticipateWithSpinLockByPhaser() throws InterruptedException {
        log.info("스핀락 테스트 시작");
        testConcurrentParticipationByPhaser("spinlock");
    }

    /**
     * Lua 락을 사용한 선착순 테스트
     */
    @Test
    void testParticipateWithLuaLockByPhaser() throws InterruptedException {
        log.info("Lua 락 테스트 시작");
        testConcurrentParticipationByPhaser("lualock");
    }

    /**
     * Redisson 락을 사용한 선착순 테스트
     */
    @Test
    void testParticipateWithRedissonLockByPhaser() throws InterruptedException {
        log.info("Redisson 락 테스트 시작");
        testConcurrentParticipationByPhaser("redisson");
    }

    /**
     * Redis INCR 명령어를 사용한 선착순 테스트
     */
    @Test
    void testParticipateWithIncrByPhaser() throws InterruptedException {
        log.info("INCR 테스트 시작");
        testConcurrentParticipationByPhaser("incr");
    }
    /**
     * 동시성 테스트 공통 메서드 - Phaser 사용 버전
     */
    private void testConcurrentParticipationByPhaser(String lockType) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        // CountDownLatch 대신 Phaser 사용
        // 메인 스레드를 첫 참여자로 등록 (1)
        Phaser phaser = new Phaser(1);
        AtomicInteger successCount = new AtomicInteger(0);
        List<String> userIds = new ArrayList<>();

        // 사용자 ID 생성
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            userIds.add("user-" + UUID.randomUUID().toString());
        }

        log.info("테스트 시작: [{}] - 동시 사용자 {} 명, 재고 {} 개", lockType, CONCURRENT_USERS, EVENT_QUANTITY);

        // 동시에 요청 보내기
        for (String userId : userIds) {
            // 각 작업 스레드를 Phaser에 등록
            phaser.register();
            executorService.submit(() -> {
                try {
                    ParticipationResult result = null;

                    switch (lockType) {
                        case "spinlock":
                            result = firstComeService.participateWithSpinLock(TEST_EVENT_ID, userId);
                            break;
                        case "lualock":
                            result = firstComeService.participateWithLuaLock(TEST_EVENT_ID, userId);
                            break;
                        case "redisson":
                            result = firstComeService.participateWithRedissonLock(TEST_EVENT_ID, userId);
                            break;
                        case "incr":
                            result = firstComeService.participateWithIncr(TEST_EVENT_ID, userId);
                            break;
                        default:
                            log.warn("지원하지 않는 락 타입: {}", lockType);
                    }

                    if (result != null && result.isSuccess()) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    // 작업 완료 후 phaser에서 제거 (CountDownLatch의 countDown() 대체)
                    phaser.arriveAndDeregister();
                }
            });
        }

        // 모든 요청이 완료될 때까지 대기
        // 타임아웃 처리를 위한 추가 코드
        int phase = phaser.getPhase();
        // 타임아웃 시간 설정
        long timeout = System.currentTimeMillis() + 30000; // 30초

        while (!phaser.isTerminated() && System.currentTimeMillis() < timeout) {
            // 현재 등록된 참여자가 1(메인 스레드)이면 모든 작업이 완료된 것
            if (phaser.getRegisteredParties() == 1) {
                break;
            }
            Thread.sleep(100); // 폴링 간격
        }

        boolean completed = phaser.getRegisteredParties() == 1;
        // 메인 스레드를 Phaser에서 해제
        phaser.arriveAndDeregister();

        executorService.shutdown();

        assertTrue(completed, "모든 요청이 시간 내에 완료되지 않았습니다.");

        // 최종 결과 확인
        var status = firstComeService.getEventStatus(TEST_EVENT_ID);
        int remainingQuantity = status.getRemainingQuantity();

        log.info("테스트 결과: [{}] - 성공 {} 건, 남은 수량 {} 개",
            lockType, successCount.get(), remainingQuantity);

        // 성공한 수와 재고 감소량이 일치하는지 확인
        assertEquals(EVENT_QUANTITY, successCount.get(),
            "성공한 건수가 재고 수량과 일치하지 않습니다.");
        assertEquals(0, remainingQuantity,
            "남은 재고가 0이 아닙니다.");

        dupTest(lockType);
    }

    public void dupTest(String lockType) {
        // 중복 참여 검증
        var testUserId = "duplicate-test-user";

        switch (lockType) {
            case "spinlock":
                var firstResult = firstComeService.participateWithSpinLock(TEST_EVENT_ID, testUserId);
                var secondResult = firstComeService.participateWithSpinLock(TEST_EVENT_ID, testUserId);

                assertTrue(!secondResult.isSuccess(),
                    "동일 사용자가 중복 참여할 수 없어야 합니다.");

                break;
            case "lualock":

                firstResult = firstComeService.participateWithLuaLock(TEST_EVENT_ID, testUserId);
                secondResult = firstComeService.participateWithLuaLock(TEST_EVENT_ID, testUserId);

                assertTrue(!secondResult.isSuccess(),
                    "동일 사용자가 중복 참여할 수 없어야 합니다.");
                break;
            case "redisson":
                firstResult = firstComeService.participateWithRedissonLock(TEST_EVENT_ID, testUserId);
                secondResult = firstComeService.participateWithRedissonLock(TEST_EVENT_ID, testUserId);

                assertTrue(!secondResult.isSuccess(),
                    "동일 사용자가 중복 참여할 수 없어야 합니다.");
                break;
            case "incr":
                firstResult = firstComeService.participateWithIncr(TEST_EVENT_ID, testUserId);
                secondResult = firstComeService.participateWithIncr(TEST_EVENT_ID, testUserId);

                assertTrue(!secondResult.isSuccess(),
                    "동일 사용자가 중복 참여할 수 없어야 합니다.");
                break;
            default:
                log.warn("지원하지 않는 락 타입: {}", lockType);
        }
    }

}
