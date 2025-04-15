package com.example.application.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Virtual Thread를 이용한 데모 서비스입니다.
 * Virtual Thread의 성능과 사용법을 보여주는 예제 메서드들을 제공합니다.
 */
@Slf4j
@Service
public class VirtualThreadDemoService {

    private final ExecutorService virtualThreadExecutor;
    private final ExecutorService platformThreadExecutor;

    public VirtualThreadDemoService(
            @Qualifier("virtualThreadExecutor") ExecutorService virtualThreadExecutor,
            @Qualifier("platformThreadExecutor") ExecutorService platformThreadExecutor) {
        this.virtualThreadExecutor = virtualThreadExecutor;
        this.platformThreadExecutor = platformThreadExecutor;
    }

    /**
     * Virtual Thread를 사용하여 여러 작업을 동시에 실행합니다.
     *
     * @param taskCount 실행할 작업의 수
     * @param sleepTimeMs 각 작업이 대기할 시간(밀리초)
     * @return 실행에 걸린 총 시간(밀리초)
     */
    public long runTasksWithVirtualThreads(int taskCount, long sleepTimeMs) {
        log.info("Virtual Thread로 {} 개의 작업 실행 시작 (각 작업 대기 시간: {}ms)", taskCount, sleepTimeMs);
        
        long startTime = System.currentTimeMillis();
        
        List<Future<?>> futures = new ArrayList<>();
        
        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            futures.add(virtualThreadExecutor.submit(() -> {
                log.debug("Virtual Thread 작업 #{} 시작", taskId);
                try {
                    Thread.sleep(sleepTimeMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                log.debug("Virtual Thread 작업 #{} 완료", taskId);
                return null;
            }));
        }
        
        // 모든 작업이 완료될 때까지 대기
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                log.error("작업 실행 중 오류 발생", e);
            }
        }
        
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        
        log.info("Virtual Thread로 {} 개의 작업 실행 완료. 총 소요 시간: {}ms", taskCount, elapsedTime);
        return elapsedTime;
    }

    /**
     * 기존 플랫폼 스레드를 사용하여 여러 작업을 동시에 실행합니다.
     * Virtual Thread와의 성능 비교를 위한 메서드입니다.
     *
     * @param taskCount 실행할 작업의 수
     * @param sleepTimeMs 각 작업이 대기할 시간(밀리초)
     * @return 실행에 걸린 총 시간(밀리초)
     */
    public long runTasksWithPlatformThreads(int taskCount, long sleepTimeMs) {
        log.info("Platform Thread로 {} 개의 작업 실행 시작 (각 작업 대기 시간: {}ms)", taskCount, sleepTimeMs);
        
        long startTime = System.currentTimeMillis();
        
        List<Future<?>> futures = new ArrayList<>();
        
        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            futures.add(platformThreadExecutor.submit(() -> {
                log.debug("Platform Thread 작업 #{} 시작", taskId);
                try {
                    Thread.sleep(sleepTimeMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                log.debug("Platform Thread 작업 #{} 완료", taskId);
                return null;
            }));
        }
        
        // 모든 작업이 완료될 때까지 대기
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                log.error("작업 실행 중 오류 발생", e);
            }
        }
        
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        
        log.info("Platform Thread로 {} 개의 작업 실행 완료. 총 소요 시간: {}ms", taskCount, elapsedTime);
        return elapsedTime;
    }
    
    /**
     * 세션 컨텍스트 정보를 가진 Virtual Thread 작업을 실행합니다.
     * 이 메서드는 세션 정보를 각 작업에 전달하여 컨텍스트를 공유합니다.
     *
     * @param taskCount 실행할 작업 수
     * @param sleepTimeMs 각 작업이 대기할 시간(밀리초)
     * @param sessionContext 세션 컨텍스트 정보
     * @return 실행에 걸린 총 시간(밀리초)
     */
    public long runTasksWithSessionContext(int taskCount, long sleepTimeMs, Map<String, Object> sessionContext) {
        log.info("세션 컨텍스트를 가진 Virtual Thread로 {} 개의 작업 실행 시작 (각 작업 대기 시간: {}ms)", 
                taskCount, sleepTimeMs);
        
        long startTime = System.currentTimeMillis();
        
        // 세션 컨텍스트 정보 로깅
        String userId = sessionContext.getOrDefault("userId", "anonymous").toString();
        String sessionId = sessionContext.getOrDefault("sessionId", "unknown").toString();
        
        log.info("세션 컨텍스트 정보 - 사용자: {}, 세션 ID: {}", userId, sessionId);
        
        // 스레드 간 공유되는 결과 저장소
        ConcurrentHashMap<Integer, String> results = new ConcurrentHashMap<>();
        
        List<Future<?>> futures = new ArrayList<>();
        
        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            futures.add(virtualThreadExecutor.submit(() -> {
                Thread currentThread = Thread.currentThread();
                log.debug("Virtual Thread 작업 #{} 시작 - 사용자: {}, 세션: {}, 스레드: {}", 
                        taskId, userId, sessionId, currentThread.getName());
                
                try {
                    // 세션 컨텍스트를 이용한 작업 시뮬레이션
                    Thread.sleep(sleepTimeMs);
                    results.put(taskId, "작업 완료 - 사용자: " + userId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    results.put(taskId, "작업 중단 - 사용자: " + userId);
                }
                
                log.debug("Virtual Thread 작업 #{} 완료 - 사용자: {}, 세션: {}", taskId, userId, sessionId);
                return null;
            }));
        }
        
        // 모든 작업이 완료될 때까지 대기
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                log.error("작업 실행 중 오류 발생 - 사용자: {}, 세션: {}", userId, sessionId, e);
            }
        }
        
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        
        log.info("세션 컨텍스트를 가진 Virtual Thread로 {} 개의 작업 실행 완료. 총 소요 시간: {}ms. 사용자: {}, 세션: {}", 
                taskCount, elapsedTime, userId, sessionId);
        
        // 결과 로깅
        log.debug("작업 결과 갯수: {}", results.size());
        
        return elapsedTime;
    }
    
    /**
     * 현재 실행 중인 스레드 정보를 반환합니다.
     *
     * @return 현재 스레드 정보
     */
    public String getCurrentThreadInfo() {
        Thread currentThread = Thread.currentThread();
        return String.format(
                "Thread Name: %s, Thread ID: %d, Virtual: %s",
                currentThread.getName(),
                currentThread.threadId(),
                currentThread.isVirtual()
        );
    }
    
    /**
     * 세션 ID로 작업 상태를 조회합니다.
     * 
     * @param sessionId 세션 ID
     * @return 해당 세션의 작업 상태 정보
     */
    public Map<String, Object> getSessionTaskStatus(String sessionId) {
        Map<String, Object> status = new ConcurrentHashMap<>();
        status.put("sessionId", sessionId);
        status.put("checkTime", System.currentTimeMillis());
        status.put("threadInfo", getCurrentThreadInfo());
        
        return status;
    }
}
