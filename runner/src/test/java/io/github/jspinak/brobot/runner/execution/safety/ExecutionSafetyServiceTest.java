package io.github.jspinak.brobot.runner.execution.safety;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.execution.context.ExecutionContext;
import io.github.jspinak.brobot.runner.execution.context.ExecutionOptions;

/**
 * Unit tests for ExecutionSafetyService.
 *
 * <p>Tests safety checks, emergency stops, action rate limiting, and mouse position validation.
 */
@DisplayName("ExecutionSafetyService Tests")
class ExecutionSafetyServiceTest {

    private ExecutionSafetyService safetyService;

    @BeforeEach
    void setUp() {
        safetyService = new ExecutionSafetyService();
    }

    @Test
    @DisplayName("Should allow actions when safety is enabled")
    void shouldAllowActionsWhenSafetyIsEnabled() {
        // Given
        ExecutionContext context = createContext("SafeTask", true);

        // When
        boolean allowed = safetyService.checkActionSafety(context, "mouse_move");

        // Then
        assertTrue(allowed);
    }

    @Test
    @DisplayName("Should bypass checks when safety is disabled")
    void shouldBypassChecksWhenSafetyIsDisabled() {
        // Given
        ExecutionContext context = createContext("UnsafeTask", false);

        // When
        boolean allowed = safetyService.checkActionSafety(context, "key_press");

        // Then
        assertTrue(allowed);
    }

    @Test
    @DisplayName("Should block actions during emergency stop")
    void shouldBlockActionsDuringEmergencyStop() {
        // Given
        ExecutionContext context = createContext("EmergencyTask", true);
        safetyService.triggerEmergencyStop("Test emergency");

        // When
        boolean allowed = safetyService.checkActionSafety(context, "mouse_move");

        // Then
        assertFalse(allowed);
        assertTrue(safetyService.isEmergencyStopped());
    }

    @Test
    @DisplayName("Should enforce action rate limits")
    void shouldEnforceActionRateLimits() {
        // Given
        ExecutionContext context = createContext("RateLimitTask", true);
        String actionType = "mouse_click";

        // When - perform actions up to the limit
        boolean allowed = true;
        int successfulActions = 0;

        // Try to perform more than 50 actions in a very short time
        for (int i = 0; i < 60 && allowed; i++) {
            allowed = safetyService.checkActionSafety(context, actionType);
            if (allowed) {
                successfulActions++;
            }
        }

        // Then - should be rate limited
        assertFalse(allowed);
        assertTrue(successfulActions <= 50); // MAX_ACTIONS_PER_SECOND
    }

    @Test
    @DisplayName("Should validate mouse position within safe bounds")
    void shouldValidateMousePositionWithinSafeBounds() {
        // Given - assuming a reasonable screen size
        // Safe bounds have 10px margin from edges

        // When/Then - valid positions
        assertTrue(safetyService.validateMousePosition(100, 100));
        assertTrue(safetyService.validateMousePosition(500, 500));

        // When/Then - invalid positions (too close to edges)
        assertFalse(safetyService.validateMousePosition(5, 100)); // Too close to left
        assertFalse(safetyService.validateMousePosition(100, 5)); // Too close to top
        assertFalse(safetyService.validateMousePosition(-10, 100)); // Negative X
        assertFalse(safetyService.validateMousePosition(100, -10)); // Negative Y
    }

    @Test
    @DisplayName("Should track consecutive failures")
    void shouldTrackConsecutiveFailures() {
        // Given
        ExecutionContext context = createContext("FailingTask", true);

        // When - record failures up to the limit
        for (int i = 0; i < 9; i++) {
            safetyService.recordFailure(context, new RuntimeException("Test failure " + i));
            assertFalse(safetyService.isEmergencyStopped());
        }

        // When - one more failure should trigger emergency stop
        safetyService.recordFailure(context, new RuntimeException("Final failure"));

        // Then
        assertTrue(safetyService.isEmergencyStopped());
    }

    @Test
    @DisplayName("Should reset failure count on success")
    void shouldResetFailureCountOnSuccess() {
        // Given
        ExecutionContext context = createContext("RecoveringTask", true);

        // Record some failures
        for (int i = 0; i < 5; i++) {
            safetyService.recordFailure(context, new RuntimeException("Test failure"));
        }

        // When - record success
        safetyService.recordSuccess(context);

        // Then - should be able to record 9 more failures before emergency stop
        for (int i = 0; i < 9; i++) {
            safetyService.recordFailure(context, new RuntimeException("Test failure"));
            assertFalse(safetyService.isEmergencyStopped());
        }

        // One more should trigger emergency stop
        safetyService.recordFailure(context, new RuntimeException("Final failure"));
        assertTrue(safetyService.isEmergencyStopped());
    }

    @Test
    @DisplayName("Should clear emergency stop")
    void shouldClearEmergencyStop() {
        // Given
        safetyService.triggerEmergencyStop("Test emergency");
        assertTrue(safetyService.isEmergencyStopped());

        // When
        safetyService.clearEmergencyStop();

        // Then
        assertFalse(safetyService.isEmergencyStopped());

        // Should allow actions again
        ExecutionContext context = createContext("ClearedTask", true);
        assertTrue(safetyService.checkActionSafety(context, "mouse_move"));
    }

    @Test
    @DisplayName("Should disable safety checks when requested")
    void shouldDisableSafetyChecksWhenRequested() {
        // Given
        safetyService.setSafetyEnabled(false);

        // When - even with emergency stop
        safetyService.triggerEmergencyStop("Test");

        // Then - should still allow actions (safety disabled)
        assertTrue(safetyService.validateMousePosition(-100, -100));

        // Re-enable safety
        safetyService.setSafetyEnabled(true);
        assertFalse(safetyService.validateMousePosition(-100, -100));
    }

    @Test
    @DisplayName("Should clean up execution tracking")
    void shouldCleanUpExecutionTracking() {
        // Given
        ExecutionContext context1 = createContext("Task1", true);
        ExecutionContext context2 = createContext("Task2", true);

        // Perform some actions
        safetyService.checkActionSafety(context1, "action1");
        safetyService.checkActionSafety(context2, "action2");

        // When
        safetyService.cleanupExecution(context1.getId());

        // Then - should still track context2 but not context1
        // We can't directly verify this without exposing internals,
        // but we can verify the service continues to function
        assertTrue(safetyService.checkActionSafety(context2, "action3"));
    }

    @Test
    @DisplayName("Should provide diagnostic information")
    void shouldProvideDiagnosticInformation() {
        // Given
        safetyService.recordFailure(createContext("FailTask", true), new RuntimeException("Test"));

        // When
        DiagnosticInfo info = safetyService.getDiagnosticInfo();

        // Then
        assertNotNull(info);
        assertEquals("ExecutionSafetyService", info.getComponent());
        assertTrue((Boolean) info.getStates().get("safetyEnabled"));
        assertFalse((Boolean) info.getStates().get("emergencyStop"));
        assertEquals(1, info.getStates().get("consecutiveFailures"));
        assertEquals(50, info.getStates().get("maxActionsPerSecond"));
        assertNotNull(info.getStates().get("safeBounds"));
    }

    @Test
    @DisplayName("Should handle different action types independently")
    void shouldHandleDifferentActionTypesIndependently() {
        // Given
        ExecutionContext context = createContext("MultiActionTask", true);

        // When - max out one action type
        for (int i = 0; i < 50; i++) {
            safetyService.checkActionSafety(context, "mouse_move");
        }

        // Then - other action types should still be allowed
        assertTrue(safetyService.checkActionSafety(context, "key_press"));
        assertTrue(safetyService.checkActionSafety(context, "mouse_click"));

        // But the maxed out type should be blocked
        assertFalse(safetyService.checkActionSafety(context, "mouse_move"));
    }

    @Test
    @DisplayName("Should enable and disable diagnostic mode")
    void shouldEnableAndDisableDiagnosticMode() {
        // Initially disabled
        assertFalse(safetyService.isDiagnosticModeEnabled());

        // Enable
        safetyService.enableDiagnosticMode(true);
        assertTrue(safetyService.isDiagnosticModeEnabled());

        // Disable
        safetyService.enableDiagnosticMode(false);
        assertFalse(safetyService.isDiagnosticModeEnabled());
    }

    @Test
    @DisplayName("Should get safe screen bounds")
    void shouldGetSafeScreenBounds() {
        // When
        DiagnosticInfo info = safetyService.getDiagnosticInfo();
        String boundsString = (String) info.getStates().get("safeBounds");

        // Then
        assertNotNull(boundsString);
        assertTrue(boundsString.contains("Rectangle"));
        // Should have margins from edges
        assertTrue(boundsString.contains("x="));
        assertTrue(boundsString.contains("y="));
        assertTrue(boundsString.contains("width="));
        assertTrue(boundsString.contains("height="));
    }

    // Helper method to create execution context
    private ExecutionContext createContext(String taskName, boolean safeMode) {
        ExecutionOptions options =
                ExecutionOptions.builder()
                        .safeMode(safeMode)
                        .timeout(Duration.ofMinutes(5))
                        .build();

        return ExecutionContext.builder()
                .taskName(taskName)
                .correlationId("test-correlation-" + taskName)
                .options(options)
                .build();
    }
}
