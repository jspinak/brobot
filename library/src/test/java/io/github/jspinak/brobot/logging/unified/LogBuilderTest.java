package io.github.jspinak.brobot.logging.unified;

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
import java.util.function.Consumer;

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
    private Consumer<LogEvent> logConsumer;
    
    @Captor
    private ArgumentCaptor<LogEvent> logEventCaptor;
    
    private LogBuilder logBuilder;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        logBuilder = new LogBuilder(logConsumer);
    }
    
    @Test
    @DisplayName("Should build and dispatch basic log event")
    void testBasicLogEvent() {
        // Act
        logBuilder
            .message("Test message")
            .log();
        
        // Assert
        verify(logConsumer).accept(logEventCaptor.capture());
        LogEvent event = logEventCaptor.getValue();
        
        assertNotNull(event);
        assertEquals("Test message", event.getMessage());
        assertEquals(LogEvent.Level.INFO, event.getLevel()); // Default level
        assertEquals(LogEvent.Type.APPLICATION, event.getType()); // Default type
        assertNotNull(event.getTimestamp());
    }
    
    @Test
    @DisplayName("Should build log event with all properties")
    void testCompleteLogEvent() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key1", "value1");
        metadata.put("key2", 42);
        
        // Act
        logBuilder
            .type(LogEvent.Type.ACTION)
            .level(LogEvent.Level.ERROR)
            .action("CLICK")
            .stateName("MainMenu")
            .objectName("LoginButton")
            .message("Click failed")
            .success(false)
            .duration(1500L)
            .metadata(metadata)
            .correlationId("test-correlation-123")
            .source("TestSource")
            .log();
        
        // Assert
        verify(logConsumer).accept(logEventCaptor.capture());
        LogEvent event = logEventCaptor.getValue();
        
        assertEquals(LogEvent.Type.ACTION, event.getType());
        assertEquals(LogEvent.Level.ERROR, event.getLevel());
        assertEquals("CLICK", event.getAction());
        assertEquals("MainMenu", event.getStateName());
        assertEquals("LoginButton", event.getObjectName());
        assertEquals("Click failed", event.getMessage());
        assertEquals(false, event.isSuccess());
        assertEquals(1500L, event.getDuration());
        assertEquals(metadata, event.getMetadata());
        assertEquals("test-correlation-123", event.getCorrelationId());
        assertEquals("TestSource", event.getSource());
    }
    
    @Test
    @DisplayName("Should support fluent chaining")
    void testFluentChaining() {
        // Act
        LogBuilder result = logBuilder
            .message("Test")
            .level(LogEvent.Level.DEBUG)
            .type(LogEvent.Type.SYSTEM)
            .action("TEST_ACTION")
            .stateName("TestState")
            .objectName("TestObject")
            .success(true)
            .duration(100L)
            .correlationId("123")
            .source("Test");
        
        // Assert - Should return same instance for chaining
        assertSame(logBuilder, result);
        
        // Verify all properties are set
        result.log();
        verify(logConsumer).accept(logEventCaptor.capture());
        LogEvent event = logEventCaptor.getValue();
        
        assertEquals("Test", event.getMessage());
        assertEquals(LogEvent.Level.DEBUG, event.getLevel());
        assertEquals(LogEvent.Type.SYSTEM, event.getType());
        assertEquals("TEST_ACTION", event.getAction());
        assertEquals("TestState", event.getStateName());
        assertEquals("TestObject", event.getObjectName());
        assertTrue(event.isSuccess());
        assertEquals(100L, event.getDuration());
        assertEquals("123", event.getCorrelationId());
        assertEquals("Test", event.getSource());
    }
    
    @Test
    @DisplayName("Should handle null values gracefully")
    void testNullValues() {
        // Act
        logBuilder
            .message(null)
            .action(null)
            .stateName(null)
            .objectName(null)
            .metadata(null)
            .correlationId(null)
            .source(null)
            .log();
        
        // Assert
        verify(logConsumer).accept(logEventCaptor.capture());
        LogEvent event = logEventCaptor.getValue();
        
        assertNull(event.getMessage());
        assertNull(event.getAction());
        assertNull(event.getStateName());
        assertNull(event.getObjectName());
        assertNull(event.getMetadata());
        assertNull(event.getCorrelationId());
        assertNull(event.getSource());
    }
    
    @Test
    @DisplayName("Should add individual metadata entries")
    void testAddMetadata() {
        // Act
        logBuilder
            .addMetadata("key1", "value1")
            .addMetadata("key2", 123)
            .addMetadata("key3", true)
            .log();
        
        // Assert
        verify(logConsumer).accept(logEventCaptor.capture());
        LogEvent event = logEventCaptor.getValue();
        
        Map<String, Object> metadata = event.getMetadata();
        assertNotNull(metadata);
        assertEquals(3, metadata.size());
        assertEquals("value1", metadata.get("key1"));
        assertEquals(123, metadata.get("key2"));
        assertEquals(true, metadata.get("key3"));
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
            .addMetadata("added1", "value3")
            .addMetadata("initial1", "overwritten") // Should overwrite
            .log();
        
        // Assert
        verify(logConsumer).accept(logEventCaptor.capture());
        LogEvent event = logEventCaptor.getValue();
        
        Map<String, Object> metadata = event.getMetadata();
        assertEquals(3, metadata.size());
        assertEquals("overwritten", metadata.get("initial1")); // Overwritten
        assertEquals("value2", metadata.get("initial2")); // Original
        assertEquals("value3", metadata.get("added1")); // Added
    }
    
    @Test
    @DisplayName("Should set different log levels")
    void testLogLevels() {
        // Test each log level
        LogEvent.Level[] levels = LogEvent.Level.values();
        
        for (LogEvent.Level level : levels) {
            // Arrange
            logBuilder = new LogBuilder(logConsumer);
            
            // Act
            logBuilder.level(level).log();
            
            // Assert
            verify(logConsumer, times(levels.length <= 5 ? 1 : 1))
                .accept(logEventCaptor.capture());
        }
        
        // Verify all captured events
        assertEquals(levels.length, logEventCaptor.getAllValues().size());
        for (int i = 0; i < levels.length; i++) {
            assertEquals(levels[i], logEventCaptor.getAllValues().get(i).getLevel());
        }
    }
    
    @Test
    @DisplayName("Should set different log types")
    void testLogTypes() {
        // Test each log type
        LogEvent.Type[] types = LogEvent.Type.values();
        
        for (LogEvent.Type type : types) {
            // Arrange
            logBuilder = new LogBuilder(logConsumer);
            
            // Act
            logBuilder.type(type).log();
            
            // Assert
            verify(logConsumer, atLeastOnce()).accept(logEventCaptor.capture());
        }
        
        // Verify all captured events
        assertEquals(types.length, logEventCaptor.getAllValues().size());
        for (int i = 0; i < types.length; i++) {
            assertEquals(types[i], logEventCaptor.getAllValues().get(i).getType());
        }
    }
    
    @Test
    @DisplayName("Should handle exception information")
    void testExceptionHandling() {
        // Arrange
        Exception testException = new RuntimeException("Test exception");
        
        // Act
        logBuilder
            .level(LogEvent.Level.ERROR)
            .message("An error occurred")
            .exception(testException)
            .log();
        
        // Assert
        verify(logConsumer).accept(logEventCaptor.capture());
        LogEvent event = logEventCaptor.getValue();
        
        assertEquals(LogEvent.Level.ERROR, event.getLevel());
        assertEquals("An error occurred", event.getMessage());
        assertEquals(testException, event.getException());
    }
    
    @Test
    @DisplayName("Should create error log convenience method")
    void testErrorConvenienceMethod() {
        // Act
        logBuilder.error("Error message", new IllegalStateException("Test"));
        
        // Assert
        verify(logConsumer).accept(logEventCaptor.capture());
        LogEvent event = logEventCaptor.getValue();
        
        assertEquals(LogEvent.Level.ERROR, event.getLevel());
        assertEquals("Error message", event.getMessage());
        assertNotNull(event.getException());
        assertEquals("Test", event.getException().getMessage());
    }
    
    @Test
    @DisplayName("Should create warning log convenience method")
    void testWarningConvenienceMethod() {
        // Act
        logBuilder.warning("Warning message");
        
        // Assert
        verify(logConsumer).accept(logEventCaptor.capture());
        LogEvent event = logEventCaptor.getValue();
        
        assertEquals(LogEvent.Level.WARNING, event.getLevel());
        assertEquals("Warning message", event.getMessage());
    }
    
    @Test
    @DisplayName("Should create info log convenience method")
    void testInfoConvenienceMethod() {
        // Act
        logBuilder.info("Info message");
        
        // Assert
        verify(logConsumer).accept(logEventCaptor.capture());
        LogEvent event = logEventCaptor.getValue();
        
        assertEquals(LogEvent.Level.INFO, event.getLevel());
        assertEquals("Info message", event.getMessage());
    }
    
    @Test
    @DisplayName("Should create debug log convenience method")
    void testDebugConvenienceMethod() {
        // Act
        logBuilder.debug("Debug message");
        
        // Assert
        verify(logConsumer).accept(logEventCaptor.capture());
        LogEvent event = logEventCaptor.getValue();
        
        assertEquals(LogEvent.Level.DEBUG, event.getLevel());
        assertEquals("Debug message", event.getMessage());
    }
    
    @Test
    @DisplayName("Should handle empty metadata map")
    void testEmptyMetadata() {
        // Act
        logBuilder
            .metadata(new HashMap<>())
            .log();
        
        // Assert
        verify(logConsumer).accept(logEventCaptor.capture());
        LogEvent event = logEventCaptor.getValue();
        
        assertNotNull(event.getMetadata());
        assertTrue(event.getMetadata().isEmpty());
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
        verify(logConsumer).accept(logEventCaptor.capture());
        LogEvent event = logEventCaptor.getValue();
        
        assertEquals(1, event.getMetadata().size());
        assertFalse(event.getMetadata().containsKey("newKey"));
    }
    
    @Test
    @DisplayName("Should reset builder after log")
    void testBuilderReset() {
        // Act - First log
        logBuilder
            .message("First message")
            .level(LogEvent.Level.ERROR)
            .log();
        
        // Act - Second log (should be reset)
        logBuilder
            .message("Second message")
            .log();
        
        // Assert
        verify(logConsumer, times(2)).accept(logEventCaptor.capture());
        
        LogEvent first = logEventCaptor.getAllValues().get(0);
        LogEvent second = logEventCaptor.getAllValues().get(1);
        
        assertEquals("First message", first.getMessage());
        assertEquals(LogEvent.Level.ERROR, first.getLevel());
        
        assertEquals("Second message", second.getMessage());
        assertEquals(LogEvent.Level.INFO, second.getLevel()); // Should be default
    }
    
    @Test
    @DisplayName("Should handle very long messages")
    void testLongMessage() {
        // Arrange
        String longMessage = "x".repeat(10000);
        
        // Act
        logBuilder.message(longMessage).log();
        
        // Assert
        verify(logConsumer).accept(logEventCaptor.capture());
        LogEvent event = logEventCaptor.getValue();
        
        assertEquals(longMessage, event.getMessage());
    }
    
    @Test
    @DisplayName("Should set action context")
    void testActionContext() {
        // Act
        logBuilder
            .actionContext("FIND", "HomePage", "SearchButton", true, 250L)
            .log();
        
        // Assert
        verify(logConsumer).accept(logEventCaptor.capture());
        LogEvent event = logEventCaptor.getValue();
        
        assertEquals("FIND", event.getAction());
        assertEquals("HomePage", event.getStateName());
        assertEquals("SearchButton", event.getObjectName());
        assertTrue(event.isSuccess());
        assertEquals(250L, event.getDuration());
        assertEquals(LogEvent.Type.ACTION, event.getType()); // Should set type to ACTION
    }
}