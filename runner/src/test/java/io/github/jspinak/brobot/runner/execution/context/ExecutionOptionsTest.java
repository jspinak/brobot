package io.github.jspinak.brobot.runner.execution.context;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ExecutionOptions class.
 *
 * <p>Tests builder pattern, default values, and factory methods.
 */
@DisplayName("ExecutionOptions Tests")
class ExecutionOptionsTest {

    @Test
    @DisplayName("Should create options with default values")
    void shouldCreateOptionsWithDefaultValues() {
        // When
        ExecutionOptions options = ExecutionOptions.defaultOptions();

        // Then
        assertEquals(Duration.ofMinutes(5), options.getTimeout());
        assertTrue(options.isSafeMode());
        assertEquals(0, options.getMaxRetries());
        assertFalse(options.isDiagnosticMode());
        assertEquals(Thread.NORM_PRIORITY, options.getPriority());
        assertTrue(options.isInterruptible());
        assertEquals(Duration.ZERO, options.getStartDelay());
    }

    @Test
    @DisplayName("Should create quick task options")
    void shouldCreateQuickTaskOptions() {
        // When
        ExecutionOptions options = ExecutionOptions.quickTask();

        // Then
        assertEquals(Duration.ofSeconds(30), options.getTimeout());
        assertFalse(options.isSafeMode());
        assertEquals(Thread.MAX_PRIORITY, options.getPriority());
        assertTrue(options.isInterruptible());
    }

    @Test
    @DisplayName("Should create long running task options")
    void shouldCreateLongRunningTaskOptions() {
        // When
        ExecutionOptions options = ExecutionOptions.longRunning();

        // Then
        assertEquals(Duration.ofHours(1), options.getTimeout());
        assertTrue(options.isSafeMode());
        assertEquals(Thread.MIN_PRIORITY, options.getPriority());
        assertFalse(options.isInterruptible());
    }

    @Test
    @DisplayName("Should create custom options with builder")
    void shouldCreateCustomOptionsWithBuilder() {
        // Given
        Duration customTimeout = Duration.ofSeconds(90);
        int customRetries = 3;
        Duration customDelay = Duration.ofSeconds(5);

        // When
        ExecutionOptions options =
                ExecutionOptions.builder()
                        .timeout(customTimeout)
                        .safeMode(false)
                        .maxRetries(customRetries)
                        .diagnosticMode(true)
                        .priority(7)
                        .interruptible(false)
                        .startDelay(customDelay)
                        .build();

        // Then
        assertEquals(customTimeout, options.getTimeout());
        assertFalse(options.isSafeMode());
        assertEquals(customRetries, options.getMaxRetries());
        assertTrue(options.isDiagnosticMode());
        assertEquals(7, options.getPriority());
        assertFalse(options.isInterruptible());
        assertEquals(customDelay, options.getStartDelay());
    }

    @Test
    @DisplayName("Should use default values for unspecified fields")
    void shouldUseDefaultValuesForUnspecifiedFields() {
        // When
        ExecutionOptions options = ExecutionOptions.builder().build();

        // Then
        assertEquals(Duration.ofMinutes(5), options.getTimeout());
        assertTrue(options.isSafeMode());
        assertEquals(0, options.getMaxRetries());
    }

    @Test
    @DisplayName("Should maintain immutability")
    void shouldMaintainImmutability() {
        // Given
        ExecutionOptions options =
                ExecutionOptions.builder().timeout(Duration.ofMinutes(10)).build();

        // When - try to get and modify (should not affect original)
        Duration timeout = options.getTimeout();
        timeout = timeout.plusMinutes(5);

        // Then - original should be unchanged
        assertEquals(Duration.ofMinutes(10), options.getTimeout());
    }
}
