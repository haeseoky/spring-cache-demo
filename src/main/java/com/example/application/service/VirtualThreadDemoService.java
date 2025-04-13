package com.example.application.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

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
}
