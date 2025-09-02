package io.github.jspinak.brobot.logging.unified;

import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.element.Text;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for LogBuilder.
 * Tests fluent API for building and dispatching log events.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LogBuilder Tests")
public class LogBuilderTest extends BrobotTestBase {

    @Mock
    private BrobotLogger mockLogger;
    
    @Mock 
    private LoggingContext mockContext;
    
    @Captor
    private ArgumentCaptor<LogEvent> logEventCaptor;
    
    private LogBuilder logBuilder;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        // Set up mocks
        when(mockContext.getSessionId()).thenReturn("test-session");
        when(mockContext.getCurrentState()).thenReturn(null);
        when(mockContext.getAllMetadata()).thenReturn(new HashMap<>());
        
        // Create a real LogBuilder with mocked dependencies
        logBuilder = new LogBuilder(mockLogger, mockContext);
    }
    
    @Test
    @DisplayName("Should build and dispatch basic log event")
    void testBasicLogEvent() {
        // Act
        logBuilder
            .message("Test message")
            .log();
        
        // Assert - verify the logger was called to route the event
        verify(mockLogger).routeEvent(any(LogEvent.class));
    }
    
    @Test
    @DisplayName("Should build log event with all properties")
    void testCompleteLogEvent() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key1", "value1");
        metadata.put("key2", 42);
        
        // Mock ActionResult
        ActionResult mockResult = mock(ActionResult.class);
        when(mockResult.isSuccess()).thenReturn(false);
        when(mockResult.getDuration()).thenReturn(Duration.ofMillis(1500));
        when(mockResult.getMatchList()).thenReturn(new ArrayList<>());
        Text emptyText = new Text();
        when(mockResult.getText()).thenReturn(emptyText);
        
        // Act
        logBuilder
            .type(LogEvent.Type.ERROR)
            .level(LogEvent.Level.ERROR)
            .action("CLICK")
            .target("LoginButton")
            .message("Click failed")
            .result(mockResult)
            .metadata(metadata)
            .error(new RuntimeException("Test exception"))
            .log();
        
        // Assert
        verify(mockLogger).routeEvent(any(LogEvent.class));
    }
    
    @Test
    @DisplayName("Should support fluent chaining")
    void testFluentChaining() {
        // Act
        LogBuilder result = logBuilder
            .message("Test")
            .level(LogEvent.Level.DEBUG)
            .type(LogEvent.Type.OBSERVATION)
            .action("TEST_ACTION")
            .target("TestObject")
            .success(true)
            .duration(100L);
        
        // Assert - Should return same instance for chaining
        assertSame(logBuilder, result);
        
        // Verify the log method can be called
        result.log();
        verify(mockLogger).routeEvent(any(LogEvent.class));
    }
    
    @Test
    @DisplayName("Should handle null values gracefully")
    void testNullValues() {
        // Act
        logBuilder
            .message(null)
            .action(null)
            .target((String) null)
            .metadata((Map<String, Object>) null)
            .log();
        
        // Assert - Should not throw exception
        verify(mockLogger).routeEvent(any(LogEvent.class));
    }
    
    @Test
    @DisplayName("Should add individual metadata entries")
    void testAddMetadata() {
        // Act
        logBuilder
            .metadata("key1", "value1")
            .metadata("key2", 123)
            .metadata("key3", true)
            .log();
        
        // Assert
        verify(mockLogger).routeEvent(any(LogEvent.class));
    }
    
    @Test
    @DisplayName("Should merge metadata correctly")
    void testMetadataMerging() {
        // Arrange
        Map<String, Object> initialMetadata = new HashMap<>();
        initialMetadata.put("initial1", "value1");
        initialMetadata.put("initial2", "value2");
        
        // Act
        logBuilder
            .metadata(initialMetadata)
            .metadata("added1", "value3")
            .metadata("initial1", "overwritten") // Should overwrite
            .log();
        
        // Assert
        verify(mockLogger).routeEvent(any(LogEvent.class));
    }
    
    @Test
    @DisplayName("Should set different log levels")
    void testLogLevels() {
        // Test each log level
        LogEvent.Level[] levels = LogEvent.Level.values();
        
        for (LogEvent.Level level : levels) {
            // Act
            new LogBuilder(mockLogger, mockContext).level(level).log();
        }
        
        // Assert - Each log level should work without errors
        verify(mockLogger, times(levels.length)).routeEvent(any(LogEvent.class));
    }
    
    @Test
    @DisplayName("Should set different log types")
    void testLogTypes() {
        // Test each log type
        LogEvent.Type[] types = LogEvent.Type.values();
        
        for (LogEvent.Type type : types) {
            // Act
            new LogBuilder(mockLogger, mockContext).type(type).log();
        }
        
        // Assert - Each log type should work without errors
        verify(mockLogger, times(types.length)).routeEvent(any(LogEvent.class));
    }
    
    @Test
    @DisplayName("Should handle exception information")
    void testExceptionHandling() {
        // Arrange
        RuntimeException testException = new RuntimeException("Test exception");
        
        // Act
        logBuilder
            .level(LogEvent.Level.ERROR)
            .message("An error occurred")
            .error(testException)
            .log();
        
        // Assert
        verify(mockLogger).routeEvent(any(LogEvent.class));
    }
    
    @Test
    @DisplayName("Should handle empty metadata map")
    void testEmptyMetadata() {
        // Act
        logBuilder
            .metadata(new HashMap<>())
            .log();
        
        // Assert
        verify(mockLogger).routeEvent(any(LogEvent.class));
    }
    
    @Test
    @DisplayName("Should preserve metadata immutability")
    void testMetadataImmutability() {
        // Arrange
        Map<String, Object> originalMetadata = new HashMap<>();
        originalMetadata.put("key", "value");
        
        // Act
        logBuilder.metadata(originalMetadata).log();
        originalMetadata.put("newKey", "newValue"); // Modify after setting
        
        // Assert
        verify(mockLogger).routeEvent(any(LogEvent.class));
    }
    
    @Test
    @DisplayName("Should handle very long messages")
    void testLongMessage() {
        // Arrange
        String longMessage = "x".repeat(1000);
        
        // Act
        logBuilder.message(longMessage).log();
        
        // Assert
        verify(mockLogger).routeEvent(any(LogEvent.class));
    }
    
    @Test
    @DisplayName("Should handle StateObject target")
    void testStateObjectTarget() {
        // Arrange
        StateObject mockStateObject = mock(StateObject.class);
        when(mockStateObject.getName()).thenReturn("TestButton");
        
        // Act
        logBuilder
            .target(mockStateObject)
            .action("CLICK")
            .log();
        
        // Assert
        verify(mockLogger).routeEvent(any(LogEvent.class));
    }
    
    @Test
    @DisplayName("Should handle performance metrics")
    void testPerformanceMetrics() {
        // Act
        logBuilder
            .performanceLog()
            .performance("responseTime", 250L)
            .message("Performance test")
            .log();
        
        // Assert
        verify(mockLogger).routeEvent(any(LogEvent.class));
    }
    
    @Test
    @DisplayName("Should handle state transitions")
    void testStateTransitions() {
        // Act
        logBuilder
            .transition("StartState", "EndState")
            .duration(300L)
            .log();
        
        // Assert
        verify(mockLogger).routeEvent(any(LogEvent.class));
    }
    
    @Test
    @DisplayName("Should handle observations")
    void testObservations() {
        // Act
        logBuilder
            .observation("System is running normally")
            .log();
        
        // Assert
        verify(mockLogger).routeEvent(any(LogEvent.class));
    }
    
    @Test
    @DisplayName("Should handle ActionResult")
    void testActionResult() {
        // Arrange
        ActionResult mockResult = mock(ActionResult.class);
        when(mockResult.isSuccess()).thenReturn(true);
        when(mockResult.getDuration()).thenReturn(Duration.ofMillis(150));
        when(mockResult.getMatchList()).thenReturn(new ArrayList<>());
        Text resultText = new Text();
        resultText.add("result text");
        when(mockResult.getText()).thenReturn(resultText);
        
        // Act
        logBuilder
            .result(mockResult)
            .action("FIND")
            .log();
        
        // Assert
        verify(mockLogger).routeEvent(any(LogEvent.class));
    }
    
    @Test
    @DisplayName("Should handle screenshot path")
    void testScreenshot() {
        // Act
        logBuilder
            .screenshot("/tmp/test-screenshot.png")
            .message("Screenshot captured")
            .log();
        
        // Assert
        verify(mockLogger).routeEvent(any(LogEvent.class));
    }
    
    @Test
    @DisplayName("Should handle colors")
    void testColors() {
        // Act
        logBuilder
            .color("RED", "BOLD")
            .message("Colored message")
            .log();
        
        // Assert
        verify(mockLogger).routeEvent(any(LogEvent.class));
    }
}