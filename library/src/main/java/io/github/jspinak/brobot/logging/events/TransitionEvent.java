package io.github.jspinak.brobot.logging.events;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import io.github.jspinak.brobot.logging.LogLevel;

import lombok.Builder;
import lombok.Value;

/**
 * Event representing a state transition in the Brobot framework.
 *
 * <p>Captures information about state changes, including the path taken, success status, and timing
 * information. This is useful for tracking navigation flows and debugging state management issues.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * TransitionEvent event = TransitionEvent.builder()
 *     .fromState("loginPage")
 *     .toState("dashboardPage")
 *     .success(true)
 *     .duration(Duration.ofSeconds(2))
 *     .method(TransitionMethod.CLICK)
 *     .build();
 * }</pre>
 */
@Value
@Builder(toBuilder = true)
public class TransitionEvent {

    /** Methods used for state transitions */
    public enum TransitionMethod {
        /** Direct navigation via clicking */
        CLICK,
        /** Navigation via typing */
        TYPE,
        /** Navigation via keyboard shortcuts */
        KEYBOARD,
        /** Navigation via URL */
        URL,
        /** Automatic transition (e.g., timeout) */
        AUTOMATIC,
        /** Programmatic transition */
        PROGRAMMATIC,
        /** Unknown or complex transition */
        OTHER
    }

    /** Timestamp when the transition was initiated */
    @Builder.Default Instant timestamp = Instant.now();

    /** Source state name */
    String fromState;

    /** Target state name */
    String toState;

    /** Whether the transition succeeded */
    boolean success;

    /** Duration of the transition */
    Duration duration;

    /** Method used for the transition */
    TransitionMethod method;

    /** Sequence of states traversed (for complex transitions) */
    @Builder.Default List<String> path = java.util.Collections.emptyList();

    /** Error message if transition failed */
    String errorMessage;

    /** Additional metadata about the transition */
    @Builder.Default Map<String, Object> metadata = java.util.Collections.emptyMap();

    /** Correlation ID for tracking related actions */
    String correlationId;

    /** Session ID */
    String sessionId;

    /** Number of retry attempts made */
    int retryAttempts;

    /**
     * Create a TransitionEvent for a successful transition.
     *
     * @param fromState The source state
     * @param toState The target state
     * @param duration How long the transition took
     * @param method The method used
     * @return A new TransitionEvent for a successful transition
     */
    public static TransitionEvent success(
            String fromState, String toState, Duration duration, TransitionMethod method) {
        return TransitionEvent.builder()
                .fromState(fromState)
                .toState(toState)
                .success(true)
                .duration(duration)
                .method(method)
                .build();
    }

    /**
     * Create a TransitionEvent for a failed transition.
     *
     * @param fromState The source state
     * @param toState The target state
     * @param duration How long the transition took
     * @param method The method used
     * @param errorMessage The error message
     * @return A new TransitionEvent for a failed transition
     */
    public static TransitionEvent failure(
            String fromState,
            String toState,
            Duration duration,
            TransitionMethod method,
            String errorMessage) {
        return TransitionEvent.builder()
                .fromState(fromState)
                .toState(toState)
                .success(false)
                .duration(duration)
                .method(method)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * Get a human-readable description of this transition event.
     *
     * @return A formatted description
     */
    public String getDescription() {
        String pathStr = path.isEmpty() ? "" : " via " + String.join(" → ", path);

        if (success) {
            return String.format(
                    "TRANSITION %s → %s [%dms] %s%s",
                    fromState, toState, duration.toMillis(), method, pathStr);
        } else {
            return String.format(
                    "TRANSITION %s → %s FAILED [%dms] %s%s%s",
                    fromState,
                    toState,
                    duration.toMillis(),
                    method,
                    pathStr,
                    errorMessage != null ? " " + errorMessage : "");
        }
    }

    /**
     * Check if this is a direct transition (no intermediate states).
     *
     * @return true if this is a direct transition
     */
    public boolean isDirect() {
        return path.isEmpty() || path.size() <= 2;
    }

    /**
     * Get the number of intermediate states in the transition path.
     *
     * @return The number of intermediate states
     */
    public int getIntermediateStateCount() {
        return Math.max(0, path.size() - 2);
    }

    /**
     * Get the log level for this transition event. Successful transitions log at INFO level,
     * failures at ERROR level.
     *
     * @return The appropriate log level
     */
    public LogLevel getLevel() {
        return success ? LogLevel.INFO : LogLevel.ERROR;
    }

    /**
     * Get a message describing this transition event.
     *
     * @return A formatted message
     */
    public String getMessage() {
        if (success) {
            return String.format("Transitioned from %s to %s", fromState, toState);
        } else {
            return String.format(
                    "Failed to transition from %s to %s%s",
                    fromState, toState, errorMessage != null ? ": " + errorMessage : "");
        }
    }

    /**
     * Get any error associated with this transition event.
     *
     * @return null since transition events only have error messages
     */
    public Throwable getError() {
        return null; // TransitionEvent only has error messages, not exceptions
    }
}
