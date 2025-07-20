package io.github.jspinak.brobot.runner.execution.context;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ExecutionContext class.
 * 
 * Tests immutability, builder pattern, and timeout calculations.
 */
@DisplayName("ExecutionContext Tests")
class ExecutionContextTest {
    
    @Test
    @DisplayName("Should create execution context with builder")
    void shouldCreateExecutionContextWithBuilder() {
        // Given
        String taskName = "TestTask";
        String correlationId = "corr-123";
        ExecutionOptions options = ExecutionOptions.defaultOptions();
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key", "value");
        
        // When
        ExecutionContext context = ExecutionContext.builder()
                .taskName(taskName)
                .correlationId(correlationId)
                .options(options)
                .metadata(metadata)
                .build();
        
        // Then
        assertNotNull(context.getId());
        assertEquals(taskName, context.getTaskName());
        assertEquals(correlationId, context.getCorrelationId());
        assertEquals(options, context.getOptions());
        assertEquals(metadata, context.getMetadata());
        assertNotNull(context.getStartTime());
    }
    
    @Test
    @DisplayName("Should generate unique ID by default")
    void shouldGenerateUniqueIdByDefault() {
        // When
        ExecutionContext context1 = ExecutionContext.builder()
                .taskName("Task1")
                .build();
        ExecutionContext context2 = ExecutionContext.builder()
                .taskName("Task2")
                .build();
        
        // Then
        assertNotNull(context1.getId());
        assertNotNull(context2.getId());
        assertNotEquals(context1.getId(), context2.getId());
    }
    
    @Test
    @DisplayName("Should calculate elapsed time correctly")
    void shouldCalculateElapsedTimeCorrectly() throws InterruptedException {
        // Given
        ExecutionContext context = ExecutionContext.builder()
                .taskName("TestTask")
                .build();
        
        // When
        Thread.sleep(100); // Sleep for 100ms
        Duration elapsed = context.getElapsedTime();
        
        // Then
        assertTrue(elapsed.toMillis() >= 100);
        assertTrue(elapsed.toMillis() < 200); // Should not be too much more
    }
    
    @Test
    @DisplayName("Should detect timeout correctly")
    void shouldDetectTimeoutCorrectly() throws InterruptedException {
        // Given
        ExecutionOptions options = ExecutionOptions.builder()
                .timeout(Duration.ofMillis(50))
                .build();
        
        ExecutionContext context = ExecutionContext.builder()
                .taskName("TestTask")
                .options(options)
                .build();
        
        // Then - initially not timed out
        assertFalse(context.isTimedOut());
        
        // When - wait for timeout
        Thread.sleep(100);
        
        // Then - should be timed out
        assertTrue(context.isTimedOut());
    }
    
    @Test
    @DisplayName("Should handle null options gracefully")
    void shouldHandleNullOptionsGracefully() {
        // Given
        ExecutionContext context = ExecutionContext.builder()
                .taskName("TestTask")
                .options(null)
                .build();
        
        // Then
        assertFalse(context.isTimedOut());
    }
    
    @Test
    @DisplayName("Should handle null timeout in options")
    void shouldHandleNullTimeoutInOptions() {
        // Given
        ExecutionOptions options = ExecutionOptions.builder()
                .timeout(null)
                .build();
        
        ExecutionContext context = ExecutionContext.builder()
                .taskName("TestTask")
                .options(options)
                .build();
        
        // Then
        assertFalse(context.isTimedOut());
    }
    
    @Test
    @DisplayName("Should set start time to current time")
    void shouldSetStartTimeToCurrentTime() {
        // Given
        Instant before = Instant.now();
        
        // When
        ExecutionContext context = ExecutionContext.builder()
                .taskName("TestTask")
                .build();
        Instant after = Instant.now();
        
        // Then
        assertNotNull(context.getStartTime());
        assertFalse(context.getStartTime().isBefore(before));
        assertFalse(context.getStartTime().isAfter(after));
    }
}