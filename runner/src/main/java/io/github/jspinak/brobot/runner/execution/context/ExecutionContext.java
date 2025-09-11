package io.github.jspinak.brobot.runner.execution.context;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Immutable context object containing all information about an execution.
 *
 * <p>This class captures the complete context of an execution operation, including identifiers,
 * timing, and configuration options.
 *
 * @since 1.0.0
 */
@Getter
@Builder
@ToString
public class ExecutionContext {

    /** Unique identifier for this execution */
    @Builder.Default private final String id = UUID.randomUUID().toString();

    /** Name of the task being executed */
    private final String taskName;

    /** Correlation ID for tracing this execution across components */
    private final String correlationId;

    /** Time when the execution started */
    @Builder.Default private final Instant startTime = Instant.now();

    /** Execution options including timeout and other settings */
    private final ExecutionOptions options;

    /** Additional metadata for the execution */
    private final Map<String, Object> metadata;

    /** Calculate the elapsed time since execution started */
    public Duration getElapsedTime() {
        return Duration.between(startTime, Instant.now());
    }

    /** Check if execution has exceeded its timeout */
    public boolean isTimedOut() {
        if (options == null || options.getTimeout() == null) {
            return false;
        }
        return getElapsedTime().compareTo(options.getTimeout()) > 0;
    }
}
