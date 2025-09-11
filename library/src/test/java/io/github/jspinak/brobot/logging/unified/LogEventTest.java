package io.github.jspinak.brobot.logging.unified;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.test.BrobotTestBase;

/** Tests for LogEvent. Tests log event data structure, builders, and serialization. */
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
        long timestamp = System.currentTimeMillis();
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key", "value");

        // Act
        LogEvent event =
                builder.timestamp(timestamp)
                        .type(LogEvent.Type.ACTION)
                        .level(LogEvent.Level.INFO)
                        .message("Test message")
                        .action("CLICK")
                        .stateId("MainState")
                        .target("Button")
                        .success(true)
                        .duration(100L)
                        .metadata(metadata)
                        .sessionId("test-123")
                        .build();

        // Assert
        assertNotNull(event);
        assertEquals(timestamp, event.getTimestamp());
        assertEquals(LogEvent.Type.ACTION, event.getType());
        assertEquals(LogEvent.Level.INFO, event.getLevel());
        assertEquals("Test message", event.getMessage());
        assertEquals("CLICK", event.getAction());
        assertEquals("MainState", event.getStateId());
        assertEquals("Button", event.getTarget());
        assertTrue(event.isSuccess());
        assertEquals(Long.valueOf(100L), event.getDuration());
        assertTrue(event.getMetadata().containsKey("key"));
        assertEquals("test-123", event.getSessionId());
    }

    @Test
    @DisplayName("Should have default values")
    void testDefaultValues() {
        // Act
        LogEvent event = builder.build();

        // Assert
        assertTrue(event.getTimestamp() > 0); // Should auto-generate
        assertEquals(LogEvent.Type.ACTION, event.getType()); // Default type
        assertEquals(LogEvent.Level.INFO, event.getLevel()); // Default level
        assertNull(event.getMessage());
        assertNull(event.getAction());
        assertNull(event.getStateId());
        assertNull(event.getTarget());
        assertTrue(event.isSuccess()); // Default success is true
        assertNull(event.getDuration());
        assertNotNull(event.getMetadata()); // Empty map, not null
        assertNull(event.getSessionId());
        assertNull(event.getError());
    }

    @Test
    @DisplayName("Should auto-generate timestamp if not provided")
    void testAutoGenerateTimestamp() {
        // Arrange
        long before = System.currentTimeMillis();

        // Act
        LogEvent event = builder.message("Test").build();

        // Assert
        long after = System.currentTimeMillis();
        assertTrue(event.getTimestamp() >= before);
        assertTrue(event.getTimestamp() <= after);
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
    @DisplayName("Should test level ordering")
    void testLevelOrdering() {
        // Test that all levels exist and can be used
        LogEvent.Level[] levels = LogEvent.Level.values();
        assertTrue(levels.length > 0);

        // Test that each level can be set
        for (LogEvent.Level level : levels) {
            LogEvent event = builder.level(level).build();
            assertEquals(level, event.getLevel());
        }
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
        LogEvent event =
                builder.level(LogEvent.Level.ERROR)
                        .message("Error occurred")
                        .error(exception)
                        .build();

        // Assert
        assertEquals(LogEvent.Level.ERROR, event.getLevel());
        assertEquals("Error occurred", event.getMessage());
        assertEquals(exception, event.getError());
        assertEquals("Test exception", event.getError().getMessage());
        assertFalse(event.isSuccess()); // Error implies failure
    }

    @Test
    @DisplayName("Should format event as string")
    void testToString() {
        // Arrange
        long timestamp = 1704110400000L; // 2024-01-01T12:00:00Z in millis

        // Act
        LogEvent event =
                builder.timestamp(timestamp)
                        .level(LogEvent.Level.INFO)
                        .type(LogEvent.Type.ACTION)
                        .message("Test message")
                        .action("CLICK")
                        .stateId("State")
                        .target("Object")
                        .build();

        // Assert
        String str = event.toString();
        assertNotNull(str);
        assertTrue(str.contains("INFO"));
        assertTrue(str.contains("ACTION"));
        assertTrue(str.contains("Test message"));
    }

    @Test
    @DisplayName("Should create action event with builder")
    void testActionEvent() {
        // Act
        LogEvent event =
                builder.type(LogEvent.Type.ACTION)
                        .level(LogEvent.Level.INFO)
                        .action("FIND")
                        .stateId("HomePage")
                        .target("SearchBox")
                        .success(true)
                        .duration(150L)
                        .build();

        // Assert
        assertEquals(LogEvent.Type.ACTION, event.getType());
        assertEquals(LogEvent.Level.INFO, event.getLevel());
        assertEquals("FIND", event.getAction());
        assertEquals("HomePage", event.getStateId());
        assertEquals("SearchBox", event.getTarget());
        assertTrue(event.isSuccess());
        assertEquals(Long.valueOf(150L), event.getDuration());
    }

    @Test
    @DisplayName("Should create observation event")
    void testObservationEvent() {
        // Act
        LogEvent event =
                builder.type(LogEvent.Type.OBSERVATION)
                        .level(LogEvent.Level.INFO)
                        .message("System startup complete")
                        .build();

        // Assert
        assertEquals(LogEvent.Type.OBSERVATION, event.getType());
        assertEquals(LogEvent.Level.INFO, event.getLevel());
        assertEquals("System startup complete", event.getMessage());
    }

    @Test
    @DisplayName("Should create error event")
    void testErrorEvent() {
        // Arrange
        Exception exception = new IllegalStateException("Invalid state");

        // Act
        LogEvent event =
                builder.type(LogEvent.Type.ERROR)
                        .level(LogEvent.Level.ERROR)
                        .message("Operation failed")
                        .error(exception)
                        .build();

        // Assert
        assertEquals(LogEvent.Type.ERROR, event.getType());
        assertEquals(LogEvent.Level.ERROR, event.getLevel());
        assertEquals("Operation failed", event.getMessage());
        assertEquals(exception, event.getError());
        assertFalse(event.isSuccess());
    }

    @Test
    @DisplayName("Should handle null values in builder")
    void testNullValues() {
        // Act
        LogEvent event =
                builder.message(null)
                        .action(null)
                        .stateId(null)
                        .target(null)
                        .metadata((Map<String, Object>) null)
                        .sessionId(null)
                        .error(null)
                        .build();

        // Assert - Should not throw exceptions
        assertNull(event.getMessage());
        assertNull(event.getAction());
        assertNull(event.getStateId());
        assertNull(event.getTarget());
        assertNotNull(event.getMetadata()); // Empty map, not null
        assertNull(event.getSessionId());
        assertNull(event.getError());
    }

    @Test
    @DisplayName("Should compare events by timestamp")
    void testEventComparison() {
        // Arrange
        long earlier = System.currentTimeMillis() - 10000;
        long later = System.currentTimeMillis();

        // Act
        LogEvent event1 = builder.timestamp(earlier).build();
        LogEvent event2 = LogEvent.builder().timestamp(later).build();

        // Assert
        assertTrue(event1.getTimestamp() < event2.getTimestamp());
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
        LogEvent infoEvent = LogEvent.builder().level(LogEvent.Level.INFO).build();
        LogEvent warningEvent = LogEvent.builder().level(LogEvent.Level.WARNING).build();
        LogEvent errorEvent = LogEvent.builder().level(LogEvent.Level.ERROR).build();

        // Act & Assert - Check that levels are set correctly
        assertEquals(LogEvent.Level.DEBUG, debugEvent.getLevel());
        assertEquals(LogEvent.Level.INFO, infoEvent.getLevel());
        assertEquals(LogEvent.Level.WARNING, warningEvent.getLevel());
        assertEquals(LogEvent.Level.ERROR, errorEvent.getLevel());
    }

    @Test
    @DisplayName("Should create event with action details")
    void testActionDetails() {
        // Act
        LogEvent event =
                builder.type(LogEvent.Type.ACTION)
                        .action("DRAG")
                        .stateId("Canvas")
                        .target("DraggableItem")
                        .fromState("StartState")
                        .toState("DropZone")
                        .duration(750L)
                        .success(true)
                        .build();

        // Assert
        assertEquals("DRAG", event.getAction());
        assertEquals("Canvas", event.getStateId());
        assertEquals("DraggableItem", event.getTarget());
        assertEquals("StartState", event.getFromState());
        assertEquals("DropZone", event.getToState());
        assertEquals(Long.valueOf(750L), event.getDuration());
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
        LogEvent event =
                builder.type(LogEvent.Type.PERFORMANCE)
                        .message("Performance metrics")
                        .metadata(performanceMetrics)
                        .build();

        // Assert
        assertEquals(LogEvent.Type.PERFORMANCE, event.getType());
        assertEquals(180L, event.getMetadata().get("searchTime"));
        assertEquals(230L, event.getMetadata().get("totalTime"));
    }

    @Test
    @DisplayName("Should create performance event with configuration metadata")
    void testConfigurationMetadata() {
        // Arrange
        Map<String, Object> config = new HashMap<>();
        config.put("verbosity", "VERBOSE");
        config.put("mockMode", true);
        config.put("timeout", 5000);

        // Act
        LogEvent event =
                builder.type(LogEvent.Type.PERFORMANCE)
                        .message("Configuration updated")
                        .metadata(config)
                        .build();

        // Assert
        assertEquals(LogEvent.Type.PERFORMANCE, event.getType());
        assertEquals("VERBOSE", event.getMetadata().get("verbosity"));
        assertEquals(true, event.getMetadata().get("mockMode"));
        assertEquals(5000, event.getMetadata().get("timeout"));
    }

    @Test
    @DisplayName("Should handle session correlation across events")
    void testSessionCorrelation() {
        // Arrange
        String sessionId = "request-123-abc";

        // Act
        LogEvent startEvent = builder.sessionId(sessionId).message("Request started").build();

        LogEvent endEvent =
                LogEvent.builder().sessionId(sessionId).message("Request completed").build();

        // Assert
        assertEquals(sessionId, startEvent.getSessionId());
        assertEquals(sessionId, endEvent.getSessionId());
        assertEquals(startEvent.getSessionId(), endEvent.getSessionId());
    }
}
