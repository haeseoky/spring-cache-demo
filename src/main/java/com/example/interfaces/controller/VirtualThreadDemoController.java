package com.example.interfaces.controller;

import com.example.application.service.VirtualThreadDemoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Virtual Thread 기능을 테스트하기 위한 API 컨트롤러입니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/thread-demo")
@RequiredArgsConstructor
public class VirtualThreadDemoController {

    private final VirtualThreadDemoService virtualThreadDemoService;

    /**
     * 현재 스레드 정보를 반환합니다.
     *
     * @return 현재 스레드 정보
     */
    @GetMapping("/info")
    public Map<String, String> getThreadInfo() {
        String threadInfo = virtualThreadDemoService.getCurrentThreadInfo();
        Map<String, String> response = new HashMap<>();
        response.put("threadInfo", threadInfo);
        return response;
    }

    /**
     * Virtual Thread와 Platform Thread 성능을 비교합니다.
     *
     * @param taskCount 실행할 작업 수 (기본값: 1000)
     * @param sleepTimeMs 각 작업의 대기 시간(밀리초) (기본값: 100)
     * @return 두 스레드 유형의 성능 비교 결과
     */
    @GetMapping("/compare")
    public Map<String, Object> compareThreads(
            @RequestParam(defaultValue = "1000") int taskCount,
            @RequestParam(defaultValue = "100") long sleepTimeMs) {
        
        long virtualThreadTime = virtualThreadDemoService.runTasksWithVirtualThreads(taskCount, sleepTimeMs);
        
        // 약간의 간격을 두어 시스템 리소스가 회복되도록 함
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long platformThreadTime = virtualThreadDemoService.runTasksWithPlatformThreads(taskCount, sleepTimeMs);
        
        Map<String, Object> result = new HashMap<>();
        result.put("taskCount", taskCount);
        result.put("sleepTimeMs", sleepTimeMs);
        result.put("virtualThreadTimeMs", virtualThreadTime);
        result.put("platformThreadTimeMs", platformThreadTime);
        result.put("timeRatio", (double) platformThreadTime / virtualThreadTime);
        result.put("virtualThreadFasterBy", (platformThreadTime > virtualThreadTime) ? 
                String.format("%.2f%%", (platformThreadTime - virtualThreadTime) * 100.0 / platformThreadTime) : 
                "Virtual Thread가 더 느림");
        
        return result;
    }

    /**
     * Virtual Thread로 작업을 실행합니다.
     *
     * @param taskCount 실행할 작업 수 (기본값: 1000)
     * @param sleepTimeMs 각 작업의 대기 시간(밀리초) (기본값: 100)
     * @return 실행 결과
     */
    @GetMapping("/virtual")
    public Map<String, Object> runVirtualThreads(
            @RequestParam(defaultValue = "1000") int taskCount,
            @RequestParam(defaultValue = "100") long sleepTimeMs) {
        
        long elapsedTime = virtualThreadDemoService.runTasksWithVirtualThreads(taskCount, sleepTimeMs);
        
        Map<String, Object> result = new HashMap<>();
        result.put("taskCount", taskCount);
        result.put("sleepTimeMs", sleepTimeMs);
        result.put("totalTimeMs", elapsedTime);
        
        return result;
    }

    /**
     * Platform Thread로 작업을 실행합니다.
     *
     * @param taskCount 실행할 작업 수 (기본값: 1000)
     * @param sleepTimeMs 각 작업의 대기 시간(밀리초) (기본값: 100)
     * @return 실행 결과
     */
    @GetMapping("/platform")
    public Map<String, Object> runPlatformThreads(
            @RequestParam(defaultValue = "1000") int taskCount,
            @RequestParam(defaultValue = "100") long sleepTimeMs) {
        
        long elapsedTime = virtualThreadDemoService.runTasksWithPlatformThreads(taskCount, sleepTimeMs);
        
        Map<String, Object> result = new HashMap<>();
        result.put("taskCount", taskCount);
        result.put("sleepTimeMs", sleepTimeMs);
        result.put("totalTimeMs", elapsedTime);
        
        return result;
    }
}
