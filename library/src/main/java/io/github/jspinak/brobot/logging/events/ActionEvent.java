package io.github.jspinak.brobot.logging.events;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.logging.LogLevel;
import io.github.jspinak.brobot.model.element.Location;

import lombok.Builder;
import lombok.Value;

/**
 * Event representing the execution of a Brobot action.
 *
 * <p>Captures all relevant information about an action execution including timing, success status,
 * location, and metadata. This event is used by the logging system to provide detailed action
 * tracking.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * ActionEvent event = ActionEvent.builder()
 *     .actionType("CLICK")
 *     .target("submitButton")
 *     .success(true)
 *     .duration(Duration.ofMillis(25))
 *     .location(new Location(100, 200))
 *     .similarity(0.95)
 *     .build();
 * }</pre>
 */
@Value
@Builder(toBuilder = true)
public class ActionEvent {

    /** Timestamp when the action was executed */
    @Builder.Default Instant timestamp = Instant.now();

    /** Type of action executed (e.g., CLICK, TYPE, FIND) */
    String actionType;

    /** Target element or description */
    String target;

    /** Whether the action succeeded */
    boolean success;

    /** How long the action took to execute */
    Duration duration;

    /** Location where the action was performed (if applicable) */
    Location location;

    /** Similarity score for pattern matching actions */
    double similarity;

    /** Error message if the action failed */
    String errorMessage;

    /** Exception if the action failed */
    Throwable error;

    /** Additional metadata about the action */
    @Builder.Default Map<String, Object> metadata = java.util.Collections.emptyMap();

    /** The action class that was executed */
    Class<? extends ActionInterface> actionClass;

    /** Correlation ID for tracking related actions */
    String correlationId;

    /** Current state when action was executed */
    String currentState;

    /** Action sequence number within a session */
    int actionSequence;

    /**
     * Create an ActionEvent for a successful action.
     *
     * @param actionType The type of action
     * @param target The target element
     * @param duration How long the action took
     * @return A new ActionEvent for a successful action
     */
    public static ActionEvent success(String actionType, String target, Duration duration) {
        return ActionEvent.builder()
                .actionType(actionType)
                .target(target)
                .success(true)
                .duration(duration)
                .build();
    }

    /**
     * Create an ActionEvent for a failed action.
     *
     * @param actionType The type of action
     * @param target The target element
     * @param duration How long the action took
     * @param errorMessage The error message
     * @return A new ActionEvent for a failed action
     */
    public static ActionEvent failure(
            String actionType, String target, Duration duration, String errorMessage) {
        return ActionEvent.builder()
                .actionType(actionType)
                .target(target)
                .success(false)
                .duration(duration)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * Get a human-readable description of this action event.
     *
     * @return A formatted description
     */
    public String getDescription() {
        if (success) {
            return String.format(
                    "%s %s → SUCCESS [%dms]%s%s",
                    actionType,
                    target,
                    duration.toMillis(),
                    location != null
                            ? String.format(" loc:(%d,%d)", location.getX(), location.getY())
                            : "",
                    similarity > 0 ? String.format(" sim:%.2f", similarity) : "");
        } else {
            return String.format(
                    "%s %s → FAILED [%dms]%s",
                    actionType,
                    target,
                    duration.toMillis(),
                    errorMessage != null ? " " + errorMessage : "");
        }
    }

    /**
     * Get the log level for this action event. Successful actions log at INFO level, failures at
     * WARN level.
     *
     * @return The appropriate log level
     */
    public LogLevel getLevel() {
        return success ? LogLevel.INFO : LogLevel.WARN;
    }

    /**
     * Get a message describing this action event.
     *
     * @return A formatted message
     */
    public String getMessage() {
        if (success) {
            return String.format("Action %s on %s completed successfully", actionType, target);
        } else {
            return String.format(
                    "Action %s on %s failed%s",
                    actionType, target, errorMessage != null ? ": " + errorMessage : "");
        }
    }

    /**
     * Get the error associated with this action event.
     *
     * @return The error, or null if none
     */
    public Throwable getError() {
        return error;
    }
}
