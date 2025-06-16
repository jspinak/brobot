package io.github.jspinak.brobot.runner.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jspinak.brobot.runner.config.ApplicationConfig;
import io.github.jspinak.brobot.runner.events.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionRecoveryManagerTest {

    @Mock
    private ApplicationConfig applicationConfig;
    
    @Mock
    private EventBus eventBus;
    
    private ObjectMapper objectMapper;
    private SessionRecoveryManager sessionManager;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // For Java 8 time support
        
        when(applicationConfig.getConfigDirectory()).thenReturn(tempDir.toFile());
        
        sessionManager = new SessionRecoveryManager(applicationConfig, eventBus, objectMapper);
    }
    
    @Test
    @DisplayName("Should initialize with new session")
    void shouldInitializeWithNewSession() {
        // Initialize
        sessionManager.initialize();
        
        // Verify new session created
        SessionState currentSession = sessionManager.getCurrentSession();
        assertNotNull(currentSession);
        assertNotNull(currentSession.getSessionId());
        assertNotNull(currentSession.getStartTime());
        assertFalse(currentSession.isEndedNormally());
    }
    
    @Test
    @DisplayName("Should save session state")
    void shouldSaveSessionState() throws Exception {
        // Initialize
        sessionManager.initialize();
        
        // Update session
        sessionManager.updateConfiguration("/path/to/config.json", true);
        sessionManager.updatePreference("theme", "dark");
        
        // Save session
        sessionManager.saveSession();
        
        // Verify file exists
        Path sessionFile = tempDir.resolve("sessions/current_session.json");
        assertTrue(Files.exists(sessionFile));
        
        // Verify content
        String content = Files.readString(sessionFile);
        assertTrue(content.contains("config.json"));
        assertTrue(content.contains("theme"));
        assertTrue(content.contains("dark"));
    }
    
    @Test
    @DisplayName("Should recover crashed session")
    void shouldRecoverCrashedSession() throws Exception {
        // Create a crashed session file
        SessionState crashedSession = SessionState.builder()
            .sessionId("test-session-123")
            .startTime(LocalDateTime.now().minusMinutes(30))
            .lastSaveTime(LocalDateTime.now().minusMinutes(5))
            .endedNormally(false)
            .applicationVersion("1.0.0")
            .openConfigurations(new ArrayList<>())
            .windowStates(new HashMap<>())
            .userPreferences(new HashMap<>())
            .executionHistory(new ArrayList<>())
            .build();
            
        crashedSession.getOpenConfigurations().add("/path/to/config.json");
        crashedSession.getUserPreferences().put("theme", "dark");
        
        // Save crashed session to disk
        Path sessionDir = tempDir.resolve("sessions");
        Files.createDirectories(sessionDir);
        Path sessionFile = sessionDir.resolve("current_session.json");
        
        String json = objectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(crashedSession);
        Files.writeString(sessionFile, json);
        
        // Initialize and recover
        sessionManager.initialize();
        
        // Verify session recovered
        SessionState currentSession = sessionManager.getCurrentSession();
        assertEquals("test-session-123", currentSession.getSessionId());
        assertTrue(currentSession.getOpenConfigurations().contains("/path/to/config.json"));
        assertEquals("dark", currentSession.getUserPreferences().get("theme"));
        
        // Verify recovery event published
        verify(eventBus).publish(any());
    }
    
    @Test
    @DisplayName("Should not recover normally ended session")
    void shouldNotRecoverNormallyEndedSession() throws Exception {
        // Create a normally ended session
        SessionState endedSession = SessionState.builder()
            .sessionId("ended-session")
            .startTime(LocalDateTime.now().minusHours(2))
            .endTime(LocalDateTime.now().minusHours(1))
            .lastSaveTime(LocalDateTime.now().minusHours(1))
            .endedNormally(true)
            .applicationVersion("1.0.0")
            .openConfigurations(new ArrayList<>())
            .windowStates(new HashMap<>())
            .userPreferences(new HashMap<>())
            .executionHistory(new ArrayList<>())
            .build();
        
        // Save to disk
        Path sessionDir = tempDir.resolve("sessions");
        Files.createDirectories(sessionDir);
        Path sessionFile = sessionDir.resolve("current_session.json");
        
        String json = objectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(endedSession);
        Files.writeString(sessionFile, json);
        
        // Initialize
        sessionManager.initialize();
        
        // Verify new session created (not recovered)
        SessionState currentSession = sessionManager.getCurrentSession();
        assertNotEquals("ended-session", currentSession.getSessionId());
    }
    
    @Test
    @DisplayName("Should archive recovered sessions")
    void shouldArchiveRecoveredSessions() throws Exception {
        // Create crashed session
        SessionState crashedSession = SessionState.builder()
            .sessionId("crashed-123")
            .startTime(LocalDateTime.now().minusMinutes(30))
            .lastSaveTime(LocalDateTime.now().minusMinutes(5))
            .endedNormally(false)
            .applicationVersion("1.0.0")
            .openConfigurations(new ArrayList<>())
            .windowStates(new HashMap<>())
            .userPreferences(new HashMap<>())
            .executionHistory(new ArrayList<>())
            .build();
        
        // Save to disk
        Path sessionDir = tempDir.resolve("sessions");
        Files.createDirectories(sessionDir);
        Path sessionFile = sessionDir.resolve("current_session.json");
        
        String json = objectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(crashedSession);
        Files.writeString(sessionFile, json);
        
        // Initialize and recover
        sessionManager.initialize();
        
        // Verify archive created
        Path recoveryDir = sessionDir.resolve("recovery");
        assertTrue(Files.exists(recoveryDir));
        
        boolean archiveFound = Files.list(recoveryDir)
            .anyMatch(p -> p.getFileName().toString().startsWith("session_"));
        assertTrue(archiveFound, "Should find archived session file");
    }
    
    @Test
    @DisplayName("Should update window states")
    void shouldUpdateWindowStates() {
        sessionManager.initialize();
        
        WindowState windowState = WindowState.builder()
            .x(100)
            .y(200)
            .width(800)
            .height(600)
            .maximized(false)
            .build();
            
        sessionManager.updateWindowState("main-window", windowState);
        
        SessionState session = sessionManager.getCurrentSession();
        assertEquals(windowState, session.getWindowStates().get("main-window"));
    }
    
    @Test
    @DisplayName("Should track execution history")
    void shouldTrackExecutionHistory() {
        sessionManager.initialize();
        
        ExecutionRecord execution = ExecutionRecord.builder()
            .executionId("exec-123")
            .configurationName("Test Config")
            .startTime(LocalDateTime.now())
            .completed(false)
            .status("Running")
            .build();
            
        sessionManager.addExecutionHistory(execution);
        
        SessionState session = sessionManager.getCurrentSession();
        assertEquals(1, session.getExecutionHistory().size());
        assertEquals("exec-123", session.getExecutionHistory().get(0).getExecutionId());
    }
    
    @Test
    @DisplayName("Should provide recovery suggestions")
    void shouldProvideRecoverySuggestions() {
        SessionState crashedSession = SessionState.builder()
            .sessionId("crashed")
            .startTime(LocalDateTime.now().minusMinutes(30))
            .lastSaveTime(LocalDateTime.now().minusMinutes(5))
            .openConfigurations(new ArrayList<>())
            .executionHistory(new ArrayList<>())
            .windowStates(new HashMap<>())
            .userPreferences(new HashMap<>())
            .build();
            
        // Add incomplete execution
        ExecutionRecord incompleteExec = ExecutionRecord.builder()
            .executionId("exec-456")
            .configurationName("Failed Config")
            .startTime(LocalDateTime.now().minusMinutes(10))
            .completed(false)
            .status("Failed")
            .build();
        crashedSession.getExecutionHistory().add(incompleteExec);
        
        RecoverySuggestions suggestions = sessionManager.getRecoverySuggestions(crashedSession);
        
        assertNotNull(suggestions);
        assertFalse(suggestions.warnings().isEmpty());
        assertTrue(suggestions.warnings().stream()
            .anyMatch(w -> w.contains("not completed")));
    }
    
    @Test
    @DisplayName("Should handle shutdown gracefully")
    void shouldHandleShutdownGracefully() {
        sessionManager.initialize();
        
        // Update session
        sessionManager.updateConfiguration("/test/config.json", true);
        
        // Shutdown
        sessionManager.shutdown();
        
        // Verify session marked as ended normally
        SessionState session = sessionManager.getCurrentSession();
        assertTrue(session.isEndedNormally());
        assertNotNull(session.getEndTime());
    }
}