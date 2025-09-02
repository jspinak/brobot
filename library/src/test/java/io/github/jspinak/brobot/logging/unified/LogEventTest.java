package io.github.jspinak.brobot.logging.unified;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LogEvent.
 * Tests log event data structure, builders, and serialization.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LogEvent Tests")
public class LogEventTest extends BrobotTestBase {

    private LogEvent.Builder builder;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        builder = LogEvent.builder();
    }
    
    @Test
    @DisplayName("Should create log event with builder")
    void testBuilderCreation() {
        // Arrange
        Instant timestamp = Instant.now();
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key", "value");
        
        // Act
        LogEvent event = builder
            .timestamp(timestamp)
            .type(LogEvent.Type.ACTION)
            .level(LogEvent.Level.INFO)
            .message("Test message")
            .action("CLICK")
            .stateName("MainState")
            .objectName("Button")
            .success(true)
            .duration(100L)
            .metadata(metadata)
            .correlationId("test-123")
            .source("TestSource")
            .build();
        
        // Assert
        assertNotNull(event);
        assertEquals(timestamp, event.getTimestamp());
        assertEquals(LogEvent.Type.ACTION, event.getType());
        assertEquals(LogEvent.Level.INFO, event.getLevel());
        assertEquals("Test message", event.getMessage());
        assertEquals("CLICK", event.getAction());
        assertEquals("MainState", event.getStateName());
        assertEquals("Button", event.getObjectName());
        assertTrue(event.isSuccess());
        assertEquals(100L, event.getDuration());
        assertEquals(metadata, event.getMetadata());
        assertEquals("test-123", event.getCorrelationId());
        assertEquals("TestSource", event.getSource());
    }
    
    @Test
    @DisplayName("Should have default values")
    void testDefaultValues() {
        // Act
        LogEvent event = builder.build();
        
        // Assert
        assertNotNull(event.getTimestamp()); // Should auto-generate
        assertEquals(LogEvent.Type.APPLICATION, event.getType()); // Default type
        assertEquals(LogEvent.Level.INFO, event.getLevel()); // Default level
        assertNull(event.getMessage());
        assertNull(event.getAction());
        assertNull(event.getStateName());
        assertNull(event.getObjectName());
        assertNull(event.isSuccess());
        assertNull(event.getDuration());
        assertNull(event.getMetadata());
        assertNull(event.getCorrelationId());
        assertNull(event.getSource());
    }
    
    @Test
    @DisplayName("Should auto-generate timestamp if not provided")
    void testAutoGenerateTimestamp() {
        // Arrange
        Instant before = Instant.now();
        
        // Act
        LogEvent event = builder.message("Test").build();
        
        // Assert
        Instant after = Instant.now();
        assertNotNull(event.getTimestamp());
        assertTrue(event.getTimestamp().compareTo(before) >= 0);
        assertTrue(event.getTimestamp().compareTo(after) <= 0);
    }
    
    @Test
    @DisplayName("Should test all log levels")
    void testLogLevels() {
        // Test each level
        for (LogEvent.Level level : LogEvent.Level.values()) {
            LogEvent event = builder.level(level).build();
            assertEquals(level, event.getLevel());
        }
    }
    
    @Test
    @DisplayName("Should test all log types")
    void testLogTypes() {
        // Test each type
        for (LogEvent.Type type : LogEvent.Type.values()) {
            LogEvent event = builder.type(type).build();
            assertEquals(type, event.getType());
        }
    }
    
    @Test
    @DisplayName("Should test level severity comparison")
    void testLevelSeverity() {
        // Assert severity order
        assertTrue(LogEvent.Level.ERROR.getSeverity() > LogEvent.Level.WARNING.getSeverity());
        assertTrue(LogEvent.Level.WARNING.getSeverity() > LogEvent.Level.INFO.getSeverity());
        assertTrue(LogEvent.Level.INFO.getSeverity() > LogEvent.Level.DEBUG.getSeverity());
        assertTrue(LogEvent.Level.DEBUG.getSeverity() > LogEvent.Level.TRACE.getSeverity());
    }
    
    @Test
    @DisplayName("Should create immutable event")
    void testImmutability() {
        // Arrange
        Map<String, Object> originalMetadata = new HashMap<>();
        originalMetadata.put("key1", "value1");
        
        // Act
        LogEvent event = builder.metadata(originalMetadata).build();
        
        // Modify original map
        originalMetadata.put("key2", "value2");
        
        // Assert - Event metadata should not be affected
        assertEquals(1, event.getMetadata().size());
        assertFalse(event.getMetadata().containsKey("key2"));
    }
    
    @Test
    @DisplayName("Should handle exception in event")
    void testExceptionHandling() {
        // Arrange
        Exception exception = new RuntimeException("Test exception");
        
        // Act
        LogEvent event = builder
            .level(LogEvent.Level.ERROR)
            .message("Error occurred")
            .exception(exception)
            .build();
        
        // Assert
        assertEquals(LogEvent.Level.ERROR, event.getLevel());
        assertEquals("Error occurred", event.getMessage());
        assertEquals(exception, event.getException());
        assertEquals("Test exception", event.getException().getMessage());
    }
    
    @Test
    @DisplayName("Should format event as string")
    void testToString() {
        // Arrange
        Instant timestamp = Instant.parse("2024-01-01T12:00:00Z");
        
        // Act
        LogEvent event = builder
            .timestamp(timestamp)
            .level(LogEvent.Level.INFO)
            .type(LogEvent.Type.ACTION)
            .message("Test message")
            .action("CLICK")
            .stateName("State")
            .objectName("Object")
            .build();
        
        // Assert
        String str = event.toString();
        assertNotNull(str);
        assertTrue(str.contains("INFO"));
        assertTrue(str.contains("ACTION"));
        assertTrue(str.contains("Test message"));
        assertTrue(str.contains("CLICK"));
        assertTrue(str.contains("State"));
        assertTrue(str.contains("Object"));
    }
    
    @Test
    @DisplayName("Should create action event")
    void testActionEvent() {
        // Act
        LogEvent event = LogEvent.action("FIND", "HomePage", "SearchBox", true, 150L);
        
        // Assert
        assertEquals(LogEvent.Type.ACTION, event.getType());
        assertEquals(LogEvent.Level.INFO, event.getLevel());
        assertEquals("FIND", event.getAction());
        assertEquals("HomePage", event.getStateName());
        assertEquals("SearchBox", event.getObjectName());
        assertTrue(event.isSuccess());
        assertEquals(150L, event.getDuration());
    }
    
    @Test
    @DisplayName("Should create system event")
    void testSystemEvent() {
        // Act
        LogEvent event = LogEvent.system("System startup complete");
        
        // Assert
        assertEquals(LogEvent.Type.SYSTEM, event.getType());
        assertEquals(LogEvent.Level.INFO, event.getLevel());
        assertEquals("System startup complete", event.getMessage());
    }
    
    @Test
    @DisplayName("Should create error event")
    void testErrorEvent() {
        // Arrange
        Exception exception = new IllegalStateException("Invalid state");
        
        // Act
        LogEvent event = LogEvent.error("Operation failed", exception);
        
        // Assert
        assertEquals(LogEvent.Type.APPLICATION, event.getType());
        assertEquals(LogEvent.Level.ERROR, event.getLevel());
        assertEquals("Operation failed", event.getMessage());
        assertEquals(exception, event.getException());
    }
    
    @Test
    @DisplayName("Should handle null values in builder")
    void testNullValues() {
        // Act
        LogEvent event = builder
            .message(null)
            .action(null)
            .stateName(null)
            .objectName(null)
            .metadata(null)
            .correlationId(null)
            .source(null)
            .exception(null)
            .build();
        
        // Assert - Should not throw exceptions
        assertNull(event.getMessage());
        assertNull(event.getAction());
        assertNull(event.getStateName());
        assertNull(event.getObjectName());
        assertNull(event.getMetadata());
        assertNull(event.getCorrelationId());
        assertNull(event.getSource());
        assertNull(event.getException());
    }
    
    @Test
    @DisplayName("Should compare events by timestamp")
    void testEventComparison() {
        // Arrange
        Instant earlier = Instant.now().minusSeconds(10);
        Instant later = Instant.now();
        
        // Act
        LogEvent event1 = builder.timestamp(earlier).build();
        LogEvent event2 = builder.timestamp(later).build();
        
        // Assert
        assertTrue(event1.getTimestamp().isBefore(event2.getTimestamp()));
    }
    
    @Test
    @DisplayName("Should handle large metadata")
    void testLargeMetadata() {
        // Arrange
        Map<String, Object> largeMetadata = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            largeMetadata.put("key" + i, "value" + i);
        }
        
        // Act
        LogEvent event = builder.metadata(largeMetadata).build();
        
        // Assert
        assertEquals(1000, event.getMetadata().size());
        assertEquals("value500", event.getMetadata().get("key500"));
    }
    
    @Test
    @DisplayName("Should test event filtering by level")
    void testLevelFiltering() {
        // Arrange
        LogEvent debugEvent = builder.level(LogEvent.Level.DEBUG).build();
        LogEvent infoEvent = builder.level(LogEvent.Level.INFO).build();
        LogEvent warningEvent = builder.level(LogEvent.Level.WARNING).build();
        LogEvent errorEvent = builder.level(LogEvent.Level.ERROR).build();
        
        // Act & Assert - Filter for WARNING and above
        int minSeverity = LogEvent.Level.WARNING.getSeverity();
        
        assertFalse(debugEvent.getLevel().getSeverity() >= minSeverity);
        assertFalse(infoEvent.getLevel().getSeverity() >= minSeverity);
        assertTrue(warningEvent.getLevel().getSeverity() >= minSeverity);
        assertTrue(errorEvent.getLevel().getSeverity() >= minSeverity);
    }
    
    @Test
    @DisplayName("Should create event with action details")
    void testActionDetails() {
        // Act
        LogEvent event = builder
            .type(LogEvent.Type.ACTION)
            .action("DRAG")
            .stateName("Canvas")
            .objectName("DraggableItem")
            .toStateName("DropZone")
            .toObjectName("Target")
            .duration(750L)
            .success(true)
            .build();
        
        // Assert
        assertEquals("DRAG", event.getAction());
        assertEquals("Canvas", event.getStateName());
        assertEquals("DraggableItem", event.getObjectName());
        assertEquals("DropZone", event.getToStateName());
        assertEquals("Target", event.getToObjectName());
        assertEquals(750L, event.getDuration());
        assertTrue(event.isSuccess());
    }
    
    @Test
    @DisplayName("Should handle performance metrics")
    void testPerformanceMetrics() {
        // Arrange
        Map<String, Object> performanceMetrics = new HashMap<>();
        performanceMetrics.put("searchTime", 180L);
        performanceMetrics.put("preprocessingTime", 30L);
        performanceMetrics.put("postprocessingTime", 20L);
        performanceMetrics.put("totalTime", 230L);
        
        // Act
        LogEvent event = builder
            .type(LogEvent.Type.PERFORMANCE)
            .message("Performance metrics")
            .metadata(performanceMetrics)
            .build();
        
        // Assert
        assertEquals(LogEvent.Type.PERFORMANCE, event.getType());
        assertEquals(180L, event.getMetadata().get("searchTime"));
        assertEquals(230L, event.getMetadata().get("totalTime"));
    }
    
    @Test
    @DisplayName("Should create configuration event")
    void testConfigurationEvent() {
        // Arrange
        Map<String, Object> config = new HashMap<>();
        config.put("verbosity", "VERBOSE");
        config.put("mockMode", true);
        config.put("timeout", 5000);
        
        // Act
        LogEvent event = builder
            .type(LogEvent.Type.CONFIGURATION)
            .message("Configuration updated")
            .metadata(config)
            .build();
        
        // Assert
        assertEquals(LogEvent.Type.CONFIGURATION, event.getType());
        assertEquals("VERBOSE", event.getMetadata().get("verbosity"));
        assertEquals(true, event.getMetadata().get("mockMode"));
        assertEquals(5000, event.getMetadata().get("timeout"));
    }
    
    @Test
    @DisplayName("Should handle correlation across events")
    void testCorrelation() {
        // Arrange
        String correlationId = "request-123-abc";
        
        // Act
        LogEvent startEvent = builder
            .correlationId(correlationId)
            .message("Request started")
            .build();
        
        LogEvent endEvent = builder
            .correlationId(correlationId)
            .message("Request completed")
            .build();
        
        // Assert
        assertEquals(correlationId, startEvent.getCorrelationId());
        assertEquals(correlationId, endEvent.getCorrelationId());
        assertEquals(startEvent.getCorrelationId(), endEvent.getCorrelationId());
    }
}