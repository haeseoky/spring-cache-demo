package com.example.application.service;

import com.example.common.test.MockSession;
import com.example.common.test.TestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestConfig.class)
class VirtualThreadDemoServiceTest {

    @Autowired
    private VirtualThreadDemoService virtualThreadDemoService;
    
    private MockHttpSession session;

    @Test
    @DisplayName("Virtual Thread 정보 확인")
    @MockSession(userId = "testUser123", userName = "테스트사용자", 
            attributeKeys = {"testAction"}, attributeValues = {"getCurrentThreadInfo"})
    void getCurrentThreadInfo() {
        String threadInfo = virtualThreadDemoService.getCurrentThreadInfo();
        System.out.println("현재 스레드 정보: " + threadInfo);
        
        // JUnit 테스트는 일반적으로 가상 스레드로 실행되지 않으므로 isVirtual은 false일 것
        assertFalse(threadInfo.contains("Virtual: true"), "JUnit 테스트 스레드는 가상 스레드가 아니어야 함");
        
        // 세션 확인
        assertEquals("testUser123", session.getAttribute("userId"));
        assertEquals("getCurrentThreadInfo", session.getAttribute("testAction"));
    }

    @Test
    @DisplayName("Virtual Thread로 직접 작업 실행 테스트")
    @MockSession(userId = "threadTester", userName = "스레드테스터", 
            attributeKeys = {"testAction", "testType"}, 
            attributeValues = {"testDirectVirtualThread", "virtual"})
    void testDirectVirtualThread() throws Exception {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            boolean isVirtual = executor.submit(() -> {
                Thread current = Thread.currentThread();
                boolean virtual = current.isVirtual();
                System.out.println("스레드 이름: " + current.getName() + ", 가상 스레드 여부: " + virtual);
                return virtual;
            }).get();
            
            assertTrue(isVirtual, "ExecutorService에서 생성된 스레드는 가상 스레드여야 함");
        }
        
        // 세션 확인
        assertEquals("threadTester", session.getAttribute("userId"));
        assertEquals("testDirectVirtualThread", session.getAttribute("testAction"));
        assertEquals("virtual", session.getAttribute("testType"));
    }

    @Test
    @DisplayName("Virtual Thread와 Platform Thread 성능 비교")
    @MockSession(userId = "performanceTester", userName = "성능테스터",
            attributeKeys = {"testAction", "taskCount", "sleepTimeMs"}, 
            attributeValues = {"compareThreadPerformance", "100", "50"})
    void compareThreadPerformance() {
        int taskCount = 100; // 테스트를 위해 적은 수의 작업
        long sleepTimeMs = 50; // 각 작업이 대기할 시간
        
        // Virtual Thread로 작업 실행
        long virtualThreadTime = virtualThreadDemoService.runTasksWithVirtualThreads(taskCount, sleepTimeMs);
        
        // Platform Thread로 작업 실행
        long platformThreadTime = virtualThreadDemoService.runTasksWithPlatformThreads(taskCount, sleepTimeMs);
        
        System.out.println("Virtual Thread 실행 시간: " + virtualThreadTime + "ms");
        System.out.println("Platform Thread 실행 시간: " + platformThreadTime + "ms");
        System.out.println("시간 비율(Platform/Virtual): " + ((double) platformThreadTime / virtualThreadTime));
        
        // 스레드 개수가 시스템의 CPU 코어 수보다 많은 경우에만 Virtual Thread가 확실히 이점이 있음
        // 테스트 환경에 따라 결과가 달라질 수 있어 단정문 사용하지 않음
        
        // 세션 확인
        assertEquals("performanceTester", session.getAttribute("userId"));
        assertEquals("compareThreadPerformance", session.getAttribute("testAction"));
        assertEquals("100", session.getAttribute("taskCount"));
    }

    @Test
    @DisplayName("대량의 Virtual Thread 작업 실행 테스트")
    @MockSession(userId = "massiveThreadTester", userName = "대량작업테스터",
            attributeKeys = {"testAction", "taskCount", "expectedFastCompletion"}, 
            attributeValues = {"runManyVirtualThreads", "10000", "true"})
    void runManyVirtualThreads() {
        int taskCount = 10000; // 1만개의 작업
        long sleepTimeMs = 10; // 각 작업당 10ms 대기
        
        long elapsedTime = virtualThreadDemoService.runTasksWithVirtualThreads(taskCount, sleepTimeMs);
        
        System.out.println("10,000개 Virtual Thread 작업 완료 시간: " + elapsedTime + "ms");
        
        // 이론적으로 Virtual Thread를 사용하면 많은 수의 작업도 효율적으로 처리할 수 있음
        // threadCount * sleepTimeMs보다 훨씬 작은 시간 내에 완료되어야 함
        assertTrue(elapsedTime < taskCount * sleepTimeMs, 
                "Virtual Thread는 작업 개수 * 대기시간보다 훨씬 빠르게 실행되어야 함");
        
        // 세션 확인
        assertEquals("massiveThreadTester", session.getAttribute("userId"));
        assertEquals("runManyVirtualThreads", session.getAttribute("testAction"));
        assertEquals("10000", session.getAttribute("taskCount"));
        assertEquals("true", session.getAttribute("expectedFastCompletion"));
    }
    
    @Test
    @DisplayName("세션 정보를 이용한 Virtual Thread 테스트")
    @MockSession(userId = "user-123", userName = "관리자",
            attributeKeys = {"userRole", "taskType", "priority"}, 
            attributeValues = {"admin", "virtualThread", "high"})
    void testWithSessionInfo() {
        int taskCount = 50;
        long sleepTimeMs = 20;
        
        // 세션 정보를 확인하며 Virtual Thread 작업 실행
        long elapsedTime = virtualThreadDemoService.runTasksWithVirtualThreads(taskCount, sleepTimeMs);
        
        System.out.println("세션 컨텍스트 정보를 가진 작업 실행 시간: " + elapsedTime + "ms");
        
        // 세션 유지 확인
        assertEquals("user-123", session.getAttribute("userId"));
        assertEquals("관리자", session.getAttribute("userName"));
        assertEquals("admin", session.getAttribute("userRole"));
        assertEquals("virtualThread", session.getAttribute("taskType"));
        assertEquals("high", session.getAttribute("priority"));
    }
}
