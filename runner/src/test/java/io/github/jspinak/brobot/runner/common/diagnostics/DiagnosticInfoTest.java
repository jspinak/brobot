package io.github.jspinak.brobot.runner.common.diagnostics;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for DiagnosticInfo class.
 *
 * <p>Tests the builder pattern, default values, and error factory method.
 */
@DisplayName("DiagnosticInfo Tests")
class DiagnosticInfoTest {

    @Test
    @DisplayName("Should create diagnostic info with builder")
    void shouldCreateDiagnosticInfoWithBuilder() {
        // Given
        String component = "TestComponent";
        String correlationId = "test-correlation-123";
        Map<String, Object> states = new HashMap<>();
        states.put("status", "running");
        states.put("count", 42);

        // When
        DiagnosticInfo info =
                DiagnosticInfo.builder()
                        .component(component)
                        .correlationId(correlationId)
                        .states(states)
                        .build();

        // Then
        assertEquals(component, info.getComponent());
        assertEquals(correlationId, info.getCorrelationId());
        assertEquals(2, info.getStates().size());
        assertEquals("running", info.getStates().get("status"));
        assertEquals(42, info.getStates().get("count"));
        assertNotNull(info.getTimestamp());
        assertTrue(info.getWarnings().isEmpty());
        assertTrue(info.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Should have default values when not specified")
    void shouldHaveDefaultValuesWhenNotSpecified() {
        // When
        DiagnosticInfo info = DiagnosticInfo.builder().component("TestComponent").build();

        // Then
        assertNotNull(info.getStates());
        assertTrue(info.getStates().isEmpty());
        assertNotNull(info.getTimestamp());
        assertNotNull(info.getWarnings());
        assertTrue(info.getWarnings().isEmpty());
        assertNotNull(info.getErrors());
        assertTrue(info.getErrors().isEmpty());
        assertNull(info.getCorrelationId());
    }

    @Test
    @DisplayName("Should create error diagnostic info")
    void shouldCreateErrorDiagnosticInfo() {
        // Given
        String component = "FailingComponent";
        Exception error = new RuntimeException("Test error message");

        // When
        DiagnosticInfo info = DiagnosticInfo.error(component, error);

        // Then
        assertEquals(component, info.getComponent());
        assertNotNull(info.getTimestamp());
        assertEquals(1, info.getErrors().size());
        assertTrue(info.getErrors().get(0).contains("RuntimeException"));
        assertTrue(info.getErrors().get(0).contains("Test error message"));
    }

    @Test
    @DisplayName("Should handle null error message gracefully")
    void shouldHandleNullErrorMessageGracefully() {
        // Given
        String component = "TestComponent";
        Exception error = new RuntimeException((String) null);

        // When
        DiagnosticInfo info = DiagnosticInfo.error(component, error);

        // Then
        assertEquals(1, info.getErrors().size());
        assertTrue(info.getErrors().get(0).contains("RuntimeException"));
    }

    @Test
    @DisplayName("Should preserve warnings and errors lists")
    void shouldPreserveWarningsAndErrorsLists() {
        // Given
        DiagnosticInfo info =
                DiagnosticInfo.builder()
                        .component("TestComponent")
                        .warnings(Arrays.asList("Warning 1", "Warning 2"))
                        .errors(Arrays.asList("Error 1", "Error 2", "Error 3"))
                        .build();

        // Then
        assertEquals(2, info.getWarnings().size());
        assertEquals("Warning 1", info.getWarnings().get(0));
        assertEquals("Warning 2", info.getWarnings().get(1));

        assertEquals(3, info.getErrors().size());
        assertEquals("Error 1", info.getErrors().get(0));
        assertEquals("Error 2", info.getErrors().get(1));
        assertEquals("Error 3", info.getErrors().get(2));
    }

    @Test
    @DisplayName("Should capture timestamp close to current time")
    void shouldCaptureTimestampCloseToCurrentTime() {
        // Given
        Instant before = Instant.now();

        // When
        DiagnosticInfo info = DiagnosticInfo.builder().component("TestComponent").build();
        Instant after = Instant.now();

        // Then
        assertNotNull(info.getTimestamp());
        assertFalse(info.getTimestamp().isBefore(before));
        assertFalse(info.getTimestamp().isAfter(after));
    }
}
