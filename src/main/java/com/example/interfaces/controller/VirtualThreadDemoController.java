package com.example.interfaces.controller;

import com.example.application.service.VirtualThreadDemoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


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
    public Map<String, String> getThreadInfo(HttpSession session) {
        // 세션에 마지막 액세스 시간 저장
        session.setAttribute("lastAccess", System.currentTimeMillis());
        
        String threadInfo = virtualThreadDemoService.getCurrentThreadInfo();
        Map<String, String> response = new HashMap<>();
        response.put("threadInfo", threadInfo);
        response.put("sessionId", session.getId());
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
            @RequestParam(defaultValue = "100") long sleepTimeMs,
            HttpSession session) {
        
        // 세션에 요청 파라미터 저장
        session.setAttribute("taskCount", taskCount);
        session.setAttribute("sleepTimeMs", sleepTimeMs);
        session.setAttribute("actionType", "compare");
        
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
        result.put("sessionId", session.getId());
        
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
            @RequestParam(defaultValue = "100") long sleepTimeMs,
            HttpSession session) {
        
        // 세션에 요청 파라미터 저장
        session.setAttribute("taskCount", taskCount);
        session.setAttribute("sleepTimeMs", sleepTimeMs);
        session.setAttribute("actionType", "virtualThread");
        
        long elapsedTime = virtualThreadDemoService.runTasksWithVirtualThreads(taskCount, sleepTimeMs);
        
        Map<String, Object> result = new HashMap<>();
        result.put("taskCount", taskCount);
        result.put("sleepTimeMs", sleepTimeMs);
        result.put("totalTimeMs", elapsedTime);
        result.put("sessionId", session.getId());
        
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
            @RequestParam(defaultValue = "100") long sleepTimeMs,
            HttpSession session) {
        
        // 세션에 요청 파라미터 저장
        session.setAttribute("taskCount", taskCount);
        session.setAttribute("sleepTimeMs", sleepTimeMs);
        session.setAttribute("actionType", "platformThread");
        
        long elapsedTime = virtualThreadDemoService.runTasksWithPlatformThreads(taskCount, sleepTimeMs);
        
        Map<String, Object> result = new HashMap<>();
        result.put("taskCount", taskCount);
        result.put("sleepTimeMs", sleepTimeMs);
        result.put("totalTimeMs", elapsedTime);
        result.put("sessionId", session.getId());
        
        return result;
    }
    
    /**
     * 세션에 사용자 정보를 설정합니다.
     *
     * @param userId 사용자 ID
     * @param userName 사용자 이름
     * @return 세션 정보
     */
    @PostMapping("/session/user")
    public Map<String, Object> setUserSession(
            @RequestParam String userId,
            @RequestParam String userName,
            HttpSession session) {
        
        // 세션에 사용자 정보 저장
        session.setAttribute("userId", userId);
        session.setAttribute("userName", userName);
        session.setAttribute("loginTime", System.currentTimeMillis());
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "사용자 세션이 설정되었습니다");
        result.put("sessionId", session.getId());
        result.put("userId", userId);
        result.put("userName", userName);
        
        return result;
    }
    
    /**
     * 현재 세션 정보를 반환합니다.
     *
     * @return 세션 정보
     */
    @GetMapping("/session/info")
    public Map<String, Object> getSessionInfo(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", session.getId());
        result.put("creationTime", session.getCreationTime());
        result.put("lastAccessedTime", session.getLastAccessedTime());
        
        // 세션에 저장된 사용자 정보가 있으면 반환
        if (session.getAttribute("userId") != null) {
            result.put("userId", session.getAttribute("userId"));
            result.put("userName", session.getAttribute("userName"));
            result.put("loginTime", session.getAttribute("loginTime"));
        }
        
        // 세션에 저장된 최근 작업 정보가 있으면 반환
        if (session.getAttribute("actionType") != null) {
            result.put("lastActionType", session.getAttribute("actionType"));
            result.put("lastTaskCount", session.getAttribute("taskCount"));
            result.put("lastSleepTimeMs", session.getAttribute("sleepTimeMs"));
        }
        
        return result;
    }
    
    /**
     * 세션을 초기화합니다.
     *
     * @return 결과 메시지
     */
    @PostMapping("/session/clear")
    public Map<String, String> clearSession(HttpSession session) {
        // 세션 무효화
        session.invalidate();
        
        Map<String, String> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "세션이 초기화되었습니다");
        
        return result;
    }
    
    /**
     * 세션 컨텍스트 정보를 이용한 Virtual Thread 작업을 실행합니다.
     *
     * @param taskCount 실행할 작업 수 (기본값: 1000)
     * @param sleepTimeMs 각 작업의 대기 시간(밀리초) (기본값: 100)
     * @return 세션 컨텍스트를 이용한 작업 실행 결과
     */
    @GetMapping("/with-session")
    public Map<String, Object> runTasksWithSessionContext(
            @RequestParam(defaultValue = "1000") int taskCount,
            @RequestParam(defaultValue = "100") long sleepTimeMs,
            HttpSession session) {
        
        // 세션에 요청 파라미터 저장
        session.setAttribute("taskCount", taskCount);
        session.setAttribute("sleepTimeMs", sleepTimeMs);
        session.setAttribute("actionType", "withSessionContext");
        session.setAttribute("startTime", System.currentTimeMillis());
        
        // 세션 컨텍스트 정보 구성
        Map<String, Object> sessionContext = new HashMap<>();
        sessionContext.put("sessionId", session.getId());
        sessionContext.put("creationTime", session.getCreationTime());
        
        // 사용자 정보가 세션에 있으면 컨텍스트에 추가
        if (session.getAttribute("userId") != null) {
            sessionContext.put("userId", session.getAttribute("userId"));
            sessionContext.put("userName", session.getAttribute("userName"));
        } else {
            // 기본 사용자 정보 설정
            sessionContext.put("userId", "guest-" + System.currentTimeMillis());
            session.setAttribute("userId", sessionContext.get("userId"));
        }
        
        // 세션 컨텍스트를 이용한 작업 실행
        long elapsedTime = virtualThreadDemoService.runTasksWithSessionContext(taskCount, sleepTimeMs, sessionContext);
        
        // 결과 저장
        session.setAttribute("completionTime", System.currentTimeMillis());
        session.setAttribute("elapsedTime", elapsedTime);
        
        Map<String, Object> result = new HashMap<>();
        result.put("taskCount", taskCount);
        result.put("sleepTimeMs", sleepTimeMs);
        result.put("totalTimeMs", elapsedTime);
        result.put("sessionId", session.getId());
        result.put("userId", sessionContext.get("userId"));
        
        return result;
    }
    
    /**
     * 세션에 연결된 작업의 상태를 조회합니다.
     *
     * @return 작업 상태 정보
     */
    @GetMapping("/session/status")
    public Map<String, Object> getSessionTaskStatus(HttpSession session) {
        String sessionId = session.getId();
        
        // 서비스를 통해 작업 상태 조회
        Map<String, Object> status = virtualThreadDemoService.getSessionTaskStatus(sessionId);
        
        // 세션에 저장된 정보 추가
        if (session.getAttribute("startTime") != null) {
            status.put("startTime", session.getAttribute("startTime"));
        }
        
        if (session.getAttribute("completionTime") != null) {
            status.put("completionTime", session.getAttribute("completionTime"));
            status.put("elapsedTime", session.getAttribute("elapsedTime"));
        }
        
        if (session.getAttribute("userId") != null) {
            status.put("userId", session.getAttribute("userId"));
        }
        
        if (session.getAttribute("actionType") != null) {
            status.put("actionType", session.getAttribute("actionType"));
            status.put("taskCount", session.getAttribute("taskCount"));
            status.put("sleepTimeMs", session.getAttribute("sleepTimeMs"));
        }
        
        return status;
    }
}
