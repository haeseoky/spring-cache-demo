package com.example.interfaces.controller;

import com.example.application.service.VirtualThreadDemoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VirtualThreadDemoController.class)
class VirtualThreadDemoControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockBean
    private VirtualThreadDemoService virtualThreadDemoService;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
        
        // 테스트용 세션 설정
        session = new MockHttpSession();
        session.setAttribute("testUser", "userTest");
        session.setAttribute("testTimestamp", System.currentTimeMillis());
        
        // Mock 서비스 설정
        when(virtualThreadDemoService.getCurrentThreadInfo())
                .thenReturn("Thread Name: Test-Thread, Thread ID: 123, Virtual: false");
        
        when(virtualThreadDemoService.runTasksWithVirtualThreads(anyInt(), anyLong()))
                .thenReturn(500L);
        
        when(virtualThreadDemoService.runTasksWithPlatformThreads(anyInt(), anyLong()))
                .thenReturn(1000L);
    }

    @Test
    @DisplayName("스레드 정보 API 테스트")
    void getThreadInfo() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/thread-demo/info")
                .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.threadInfo").exists())
                .andExpect(jsonPath("$.threadInfo").value("Thread Name: Test-Thread, Thread ID: 123, Virtual: false"))
                .andReturn();
        
        // 세션 유지 확인
        MockHttpSession resultSession = (MockHttpSession) result.getRequest().getSession();
        assert resultSession.getAttribute("testUser").equals("userTest");
    }

    @Test
    @DisplayName("스레드 성능 비교 API 테스트")
    void compareThreads() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/thread-demo/compare")
                .param("taskCount", "1000")
                .param("sleepTimeMs", "100")
                .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskCount").value(1000))
                .andExpect(jsonPath("$.sleepTimeMs").value(100))
                .andExpect(jsonPath("$.virtualThreadTimeMs").value(500))
                .andExpect(jsonPath("$.platformThreadTimeMs").value(1000))
                .andExpect(jsonPath("$.timeRatio").value(2.0))
                .andExpect(jsonPath("$.virtualThreadFasterBy").value("50.00%"))
                .andReturn();
        
        // 세션 유지 확인
        MockHttpSession resultSession = (MockHttpSession) result.getRequest().getSession();
        assert resultSession.getAttribute("testUser").equals("userTest");
    }

    @Test
    @DisplayName("Virtual Thread 성능 API 테스트")
    void runVirtualThreads() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/thread-demo/virtual")
                .param("taskCount", "1000")
                .param("sleepTimeMs", "100")
                .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskCount").value(1000))
                .andExpect(jsonPath("$.sleepTimeMs").value(100))
                .andExpect(jsonPath("$.totalTimeMs").value(500))
                .andReturn();
        
        // 세션 유지 확인
        MockHttpSession resultSession = (MockHttpSession) result.getRequest().getSession();
        assert resultSession.getAttribute("testUser").equals("userTest");
    }

    @Test
    @DisplayName("Platform Thread 성능 API 테스트")
    void runPlatformThreads() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/thread-demo/platform")
                .param("taskCount", "1000")
                .param("sleepTimeMs", "100")
                .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskCount").value(1000))
                .andExpect(jsonPath("$.sleepTimeMs").value(100))
                .andExpect(jsonPath("$.totalTimeMs").value(1000))
                .andReturn();
        
        // 세션 유지 확인
        MockHttpSession resultSession = (MockHttpSession) result.getRequest().getSession();
        assert resultSession.getAttribute("testUser").equals("userTest");
    }
    
    @Test
    @DisplayName("세션 컨텍스트를 이용한 Virtual Thread 작업 API 테스트")
    void runTasksWithSessionContext() throws Exception {
        // 세션에 사용자 정보 설정
        session.setAttribute("userId", "test-user-123");
        session.setAttribute("userName", "테스트사용자");
        
        when(virtualThreadDemoService.runTasksWithSessionContext(anyInt(), anyLong(), any()))
                .thenReturn(600L);
        
        MvcResult result = mockMvc.perform(get("/api/thread-demo/with-session")
                .param("taskCount", "500")
                .param("sleepTimeMs", "50")
                .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskCount").value(500))
                .andExpect(jsonPath("$.sleepTimeMs").value(50))
                .andExpect(jsonPath("$.totalTimeMs").value(600))
                .andExpect(jsonPath("$.userId").value("test-user-123"))
                .andReturn();
        
        // 세션 유지 및 업데이트 확인
        MockHttpSession resultSession = (MockHttpSession) result.getRequest().getSession();
        assert resultSession.getAttribute("userId").equals("test-user-123");
        assert resultSession.getAttribute("actionType").equals("withSessionContext");
        assert resultSession.getAttribute("taskCount").equals(500);
        assert resultSession.getAttribute("sleepTimeMs").equals(50L);
        assert resultSession.getAttribute("elapsedTime").equals(600L);
    }
    
    @Test
    @DisplayName("세션 작업 상태 조회 API 테스트")
    void getSessionTaskStatus() throws Exception {
        // 세션에 작업 정보 설정
        session.setAttribute("actionType", "testAction");
        session.setAttribute("taskCount", 1000);
        session.setAttribute("sleepTimeMs", 100L);
        session.setAttribute("startTime", System.currentTimeMillis() - 2000);
        session.setAttribute("completionTime", System.currentTimeMillis() - 1000);
        session.setAttribute("elapsedTime", 1000L);
        session.setAttribute("userId", "test-user-123");
        
        when(virtualThreadDemoService.getSessionTaskStatus(anyString()))
                .thenReturn(Map.of(
                        "sessionId", "test-session-id",
                        "checkTime", System.currentTimeMillis(),
                        "threadInfo", "Thread Name: Test-Thread, Thread ID: 123, Virtual: false"
                ));
        
        MvcResult result = mockMvc.perform(get("/api/thread-demo/session/status")
                .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").exists())
                .andExpect(jsonPath("$.checkTime").exists())
                .andExpect(jsonPath("$.threadInfo").exists())
                .andExpect(jsonPath("$.actionType").value("testAction"))
                .andExpect(jsonPath("$.taskCount").value(1000))
                .andExpect(jsonPath("$.sleepTimeMs").value(100))
                .andExpect(jsonPath("$.elapsedTime").value(1000))
                .andExpect(jsonPath("$.userId").value("test-user-123"))
                .andReturn();
        
        // 세션 유지 확인
        MockHttpSession resultSession = (MockHttpSession) result.getRequest().getSession();
        assert resultSession.getAttribute("userId").equals("test-user-123");
    }
    
    @Test
    @DisplayName("세션 사용자 설정 API 테스트")
    void setUserSession() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/thread-demo/session/user")
                .param("userId", "new-user-456")
                .param("userName", "새사용자")
                .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value("new-user-456"))
                .andExpect(jsonPath("$.userName").value("새사용자"))
                .andReturn();
        
        // 세션 업데이트 확인
        MockHttpSession resultSession = (MockHttpSession) result.getRequest().getSession();
        assert resultSession.getAttribute("userId").equals("new-user-456");
        assert resultSession.getAttribute("userName").equals("새사용자");
        assert resultSession.getAttribute("loginTime") != null;
    }
}
