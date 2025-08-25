package io.github.jspinak.brobot.logging.unified;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.logging.DiagnosticLogger;
import io.github.jspinak.brobot.logging.ActionLogger;
import io.github.jspinak.brobot.logging.modular.ActionLoggingService;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.logging.ConsoleOutputManager;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.logging.unified.console.ConsoleFormatter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageRouterTest extends BrobotTestBase {
    
    private MessageRouter messageRouter;
    
    @Mock
    private ActionLogger actionLogger;
    
    @Mock
    private ConsoleReporter consoleReporter;
    
    @Mock
    private ConsoleFormatter consoleFormatter;
    
    @Mock
    private ActionLoggingService actionLoggingService;
    
    @Mock
    private DiagnosticLogger diagnosticLogger;
    
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        
        // Capture console output
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        
        // Create MessageRouter with mocked dependencies
        messageRouter = new MessageRouter(actionLogger, consoleReporter, 
                                         consoleFormatter, actionLoggingService);
        
        // Setup default formatter behavior
        when(consoleFormatter.format(any(LogEvent.class))).thenAnswer(invocation -> {
            LogEvent event = invocation.getArgument(0);
            return String.format("[%s] %s", event.getLevel(), event.getMessage());
        });
        
        // Setup console reporter level checking
        when(consoleReporter.levelAllows(any())).thenReturn(true);
    }
    
    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
    }
    
    @Test
    public void testRouteEvent_ActionEvent() {
        // Arrange
        FrameworkSettings.structuredLogging = true;
        LoggingContext.setSessionId("test-session");
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("actionType", "CLICK");
        metadata.put("target", "button");
        
        LogEvent event = LogEvent.builder()
            .type(LogEvent.Type.ACTION)
            .level(LogEvent.Level.INFO)
            .message("Click action performed")
            .metadata(metadata)
            .success(true)
            .timestamp(Instant.now())
            .build();
        
        // Act
        messageRouter.routeEvent(event);
        
        // Assert
        verify(actionLogger).logAction(anyString(), any(ActionResult.class));
        verify(consoleFormatter).format(event);
        assertTrue(outputStream.toString().contains("[INFO]"));
    }
    
    @Test
    public void testRouteEvent_TransitionEvent() {
        // Arrange
        FrameworkSettings.structuredLogging = true;
        LoggingContext.setSessionId("test-session");
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("fromState", "Login");
        metadata.put("toState", "Dashboard");
        
        LogEvent event = LogEvent.builder()
            .type(LogEvent.Type.TRANSITION)
            .level(LogEvent.Level.INFO)
            .message("State transition")
            .metadata(metadata)
            .success(true)
            .timestamp(Instant.now())
            .build();
        
        // Act
        messageRouter.routeEvent(event);
        
        // Assert
        verify(actionLogger).logStateTransition(anyString(), any(), any(), anyBoolean(), anyLong());
        verify(consoleFormatter).format(event);
    }
    
    @Test
    public void testRouteEvent_ObservationEvent() {
        // Arrange
        FrameworkSettings.structuredLogging = true;
        LoggingContext.setSessionId("test-session");
        
        LogEvent event = LogEvent.builder()
            .type(LogEvent.Type.OBSERVATION)
            .level(LogEvent.Level.INFO)
            .message("Test observation")
            .timestamp(Instant.now())
            .build();
        
        // Act
        messageRouter.routeEvent(event);
        
        // Assert
        verify(actionLogger).logObservation(anyString(), eq("Test observation"));
        verify(consoleFormatter).format(event);
    }
    
    @Test
    public void testRouteEvent_PerformanceEvent() {
        // Arrange
        FrameworkSettings.structuredLogging = true;
        LoggingContext.setSessionId("test-session");
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("duration", 1500L);
        metadata.put("operation", "findImage");
        
        LogEvent event = LogEvent.builder()
            .type(LogEvent.Type.PERFORMANCE)
            .level(LogEvent.Level.INFO)
            .message("Performance metrics")
            .metadata(metadata)
            .timestamp(Instant.now())
            .build();
        
        // Act
        messageRouter.routeEvent(event);
        
        // Assert
        verify(actionLogger).logPerformanceMetrics(anyString(), any());
        verify(consoleFormatter).format(event);
    }
    
    @Test
    public void testRouteEvent_ErrorEvent() {
        // Arrange
        FrameworkSettings.structuredLogging = true;
        LoggingContext.setSessionId("test-session");
        
        Exception error = new RuntimeException("Test error");
        
        LogEvent event = LogEvent.builder()
            .type(LogEvent.Type.ERROR)
            .level(LogEvent.Level.ERROR)
            .message("Error occurred")
            .error(error)
            .timestamp(Instant.now())
            .build();
        
        // Act
        messageRouter.routeEvent(event);
        
        // Assert
        verify(actionLogger).logError(anyString(), eq("Error occurred"), eq(error));
        verify(consoleFormatter).format(event);
    }
    
    @Test
    public void testRouteEvent_WithoutSessionId() {
        // Arrange
        FrameworkSettings.structuredLogging = true;
        LoggingContext.clearSession(); // No session ID
        
        LogEvent event = LogEvent.builder()
            .type(LogEvent.Type.ACTION)
            .level(LogEvent.Level.INFO)
            .message("Action without session")
            .timestamp(Instant.now())
            .build();
        
        // Act
        messageRouter.routeEvent(event);
        
        // Assert
        // Should only route to console, not ActionLogger
        verify(actionLogger, never()).logAction(anyString(), any());
        verify(consoleFormatter).format(event);
    }
    
    @Test
    public void testRouteEvent_StructuredLoggingDisabled() {
        // Arrange
        FrameworkSettings.structuredLogging = false;
        
        LogEvent event = LogEvent.builder()
            .type(LogEvent.Type.ACTION)
            .level(LogEvent.Level.INFO)
            .message("Action with structured logging disabled")
            .timestamp(Instant.now())
            .build();
        
        // Act
        messageRouter.routeEvent(event);
        
        // Assert
        // Should only route to console
        verify(actionLogger, never()).logAction(anyString(), any());
        verify(consoleFormatter).format(event);
    }
    
    @Test
    public void testDetermineConsoleLevel_ErrorType() {
        // Arrange
        LogEvent event = LogEvent.builder()
            .type(LogEvent.Type.ERROR)
            .level(LogEvent.Level.ERROR)
            .message("Error message")
            .build();
        
        // Act
        ConsoleOutputManager.OutputLevel level = invokePrivateMethod(
            messageRouter, "determineConsoleLevel", event);
        
        // Assert
        assertEquals(ConsoleOutputManager.OutputLevel.LOW, level);
    }
    
    @Test
    public void testDetermineConsoleLevel_ActionType() {
        // Arrange
        LogEvent event = LogEvent.builder()
            .type(LogEvent.Type.ACTION)
            .level(LogEvent.Level.INFO)
            .message("Action message")
            .build();
        
        // Act
        ConsoleOutputManager.OutputLevel level = invokePrivateMethod(
            messageRouter, "determineConsoleLevel", event);
        
        // Assert
        assertEquals(ConsoleOutputManager.OutputLevel.LOW, level);
    }
    
    @Test
    public void testDetermineConsoleLevel_ObservationType() {
        // Arrange
        LogEvent event = LogEvent.builder()
            .type(LogEvent.Type.OBSERVATION)
            .level(LogEvent.Level.INFO)
            .message("Observation message")
            .build();
        
        // Act
        ConsoleOutputManager.OutputLevel level = invokePrivateMethod(
            messageRouter, "determineConsoleLevel", event);
        
        // Assert
        assertEquals(ConsoleOutputManager.OutputLevel.HIGH, level);
    }
    
    @Test
    public void testRouteToConsole_WithLevelCheck() {
        // Arrange
        LogEvent event = LogEvent.builder()
            .type(LogEvent.Type.ACTION)
            .level(LogEvent.Level.INFO)
            .message("Test action")
            .timestamp(Instant.now())
            .build();
        
        when(consoleReporter.levelAllows(ConsoleOutputManager.OutputLevel.LOW))
            .thenReturn(true);
        
        // Act
        invokePrivateMethod(messageRouter, "routeToConsole", event);
        
        // Assert
        verify(consoleFormatter).format(event);
        assertTrue(outputStream.toString().contains("Test action"));
    }
    
    @Test
    public void testRouteToConsole_LevelNotAllowed() {
        // Arrange
        LogEvent event = LogEvent.builder()
            .type(LogEvent.Type.OBSERVATION)
            .level(LogEvent.Level.DEBUG)
            .message("Debug observation")
            .timestamp(Instant.now())
            .build();
        
        when(consoleReporter.levelAllows(ConsoleOutputManager.OutputLevel.HIGH))
            .thenReturn(false);
        
        // Act
        invokePrivateMethod(messageRouter, "routeToConsole", event);
        
        // Assert
        verify(consoleFormatter, never()).format(event);
        assertTrue(outputStream.toString().isEmpty());
    }
    
    @Test
    public void testRouteToActionLogger_CreateActionResult() {
        // Arrange
        FrameworkSettings.structuredLogging = true;
        LoggingContext.setSessionId("test-session");
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("actionType", "FIND");
        metadata.put("duration", 500L);
        metadata.put("matchCount", 3);
        
        LogEvent event = LogEvent.builder()
            .type(LogEvent.Type.ACTION)
            .level(LogEvent.Level.INFO)
            .message("Find action")
            .metadata(metadata)
            .success(true)
            .timestamp(Instant.now())
            .build();
        
        // Act
        invokePrivateMethod(messageRouter, "routeToActionLogger", event);
        
        // Assert
        ArgumentCaptor<ActionResult> resultCaptor = ArgumentCaptor.forClass(ActionResult.class);
        verify(actionLogger).logAction(eq("test-session"), resultCaptor.capture());
        
        ActionResult capturedResult = resultCaptor.getValue();
        assertTrue(capturedResult.isSuccess());
        assertEquals(500L, capturedResult.getDuration());
    }
    
    @Test
    public void testFormatMessage_VerboseMode() {
        // Arrange
        FrameworkSettings.consoleVerbosity = BrobotLogger.Verbosity.VERBOSE;
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key1", "value1");
        metadata.put("key2", 123);
        
        LogEvent event = LogEvent.builder()
            .type(LogEvent.Type.ACTION)
            .level(LogEvent.Level.INFO)
            .message("Test message")
            .metadata(metadata)
            .timestamp(Instant.now())
            .build();
        
        when(consoleFormatter.format(event)).thenReturn(
            "[INFO] Test message | key1=value1, key2=123");
        
        // Act
        String formatted = invokePrivateMethod(messageRouter, "formatMessage", event);
        
        // Assert
        assertNotNull(formatted);
        assertTrue(formatted.contains("Test message"));
    }
    
    @Test
    public void testFormatMessage_NormalMode() {
        // Arrange
        FrameworkSettings.consoleVerbosity = BrobotLogger.Verbosity.NORMAL;
        
        LogEvent event = LogEvent.builder()
            .type(LogEvent.Type.ACTION)
            .level(LogEvent.Level.INFO)
            .message("Test message")
            .objectName("TestObject")
            .timestamp(Instant.now())
            .build();
        
        when(consoleFormatter.format(event)).thenReturn("[INFO] Test message");
        
        // Act
        String formatted = invokePrivateMethod(messageRouter, "formatMessage", event);
        
        // Assert
        assertNotNull(formatted);
        assertTrue(formatted.contains("Test message"));
    }
    
    @Test
    public void testHandleNullEvent() {
        // Act & Assert
        assertDoesNotThrow(() -> messageRouter.routeEvent(null));
        
        // Verify no interactions with dependencies
        verifyNoInteractions(actionLogger);
        verifyNoInteractions(consoleFormatter);
    }
    
    @Test
    public void testConcurrentEventRouting() throws InterruptedException {
        // Arrange
        FrameworkSettings.structuredLogging = true;
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        
        // Act
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                LoggingContext.setSessionId("session-" + index);
                
                LogEvent event = LogEvent.builder()
                    .type(LogEvent.Type.OBSERVATION)
                    .level(LogEvent.Level.INFO)
                    .message("Concurrent message " + index)
                    .timestamp(Instant.now())
                    .build();
                
                messageRouter.routeEvent(event);
            });
            threads[i].start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Assert
        verify(actionLogger, times(threadCount)).logObservation(anyString(), anyString());
        verify(consoleFormatter, times(threadCount)).format(any(LogEvent.class));
    }
    
    @Test
    public void testStateTransitionWithNullStates() {
        // Arrange
        FrameworkSettings.structuredLogging = true;
        LoggingContext.setSessionId("test-session");
        
        Map<String, Object> metadata = new HashMap<>();
        // No fromState or toState in metadata
        
        LogEvent event = LogEvent.builder()
            .type(LogEvent.Type.TRANSITION)
            .level(LogEvent.Level.INFO)
            .message("State transition with null states")
            .metadata(metadata)
            .timestamp(Instant.now())
            .build();
        
        // Act
        messageRouter.routeEvent(event);
        
        // Assert
        verify(actionLogger).logStateTransition(eq("test-session"), 
            isNull(), isNull(), anyBoolean(), anyLong());
    }
    
    private <T> T invokePrivateMethod(Object target, String methodName, Object... args) {
        try {
            return (T) ReflectionTestUtils.invokeMethod(target, methodName, args);
        } catch (Exception e) {
            fail("Failed to invoke private method: " + methodName + " - " + e.getMessage());
            return null;
        }
    }
}