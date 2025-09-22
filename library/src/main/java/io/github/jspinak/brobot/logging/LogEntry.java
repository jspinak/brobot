package io.github.jspinak.brobot.logging;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.Duration;
import java.util.Map;
import java.util.HashMap;

/**
 * The actual log entry data structure that gets passed to formatters.
 *
 * <p>This immutable data structure contains all the information needed
 * to format and output a log entry. It serves as the bridge between
 * the high-level logging API and the various formatters.
 *
 * <p>Features:
 * <ul>
 *   <li>Immutable design for thread safety
 *   <li>Rich metadata support with type safety
 *   <li>Correlation and timing information
 *   <li>Action-specific context
 *   <li>Error and performance data
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * LogEntry entry = LogEntry.builder()
 *     .timestamp(Instant.now())
 *     .category(LogCategory.ACTIONS)
 *     .level(LogLevel.INFO)
 *     .message("Clicked submit button")
 *     .correlationId("abc123")
 *     .actionType("CLICK")
 *     .actionTarget("submitButton")
 *     .success(true)
 *     .duration(Duration.ofMillis(25))
 *     .build();
 * }</pre>
 */
@Data
@Builder(toBuilder = true)
public class LogEntry {

    // Core log entry fields
    private final Instant timestamp;
    private final LogCategory category;
    private final LogLevel level;
    private final String message;
    private final String threadName;

    // Correlation and context
    private final String correlationId;
    private final String sessionId;
    private final String operationName;

    // Action-specific fields
    private final String actionType;
    private final String actionTarget;
    private final Boolean success;
    private final Double similarity;
    private final io.github.jspinak.brobot.model.element.Location location;

    // Timing information
    private final Duration duration;
    private final Instant operationStartTime;
    private final Long operationDepth;

    // Performance metrics
    private final Long memoryUsage;
    private final Long operationCount;

    // Error information
    private final Throwable error;
    private final String errorMessage;

    // State context
    private final String currentState;
    private final String targetState;

    // Custom metadata
    @Builder.Default
    private final Map<String, Object> metadata = new HashMap<>();

    /**
     * Create a simple log entry with just the essential fields.
     *
     * @param category The log category
     * @param level The log level
     * @param message The log message
     * @return A new LogEntry instance
     */
    public static LogEntry simple(LogCategory category, LogLevel level, String message) {
        return LogEntry.builder()
                .timestamp(Instant.now())
                .category(category)
                .level(level)
                .message(message)
                .threadName(Thread.currentThread().getName())
                .build();
    }

    /**
     * Create a log entry for an action event.
     *
     * @param category The log category
     * @param level The log level
     * @param message The log message
     * @param actionType The type of action performed
     * @param actionTarget The target of the action
     * @param success Whether the action succeeded
     * @param duration How long the action took
     * @return A new LogEntry instance
     */
    public static LogEntry forAction(LogCategory category, LogLevel level, String message,
                                   String actionType, String actionTarget, Boolean success, Duration duration) {
        return LogEntry.builder()
                .timestamp(Instant.now())
                .category(category)
                .level(level)
                .message(message)
                .threadName(Thread.currentThread().getName())
                .actionType(actionType)
                .actionTarget(actionTarget)
                .success(success)
                .duration(duration)
                .build();
    }

    /**
     * Create a log entry for an error condition.
     *
     * @param category The log category
     * @param message The log message
     * @param error The error/exception
     * @return A new LogEntry instance
     */
    public static LogEntry forError(LogCategory category, String message, Throwable error) {
        return LogEntry.builder()
                .timestamp(Instant.now())
                .category(category)
                .level(LogLevel.ERROR)
                .message(message)
                .threadName(Thread.currentThread().getName())
                .error(error)
                .errorMessage(error != null ? error.getMessage() : null)
                .build();
    }

    /**
     * Create a log entry for performance metrics.
     *
     * @param message The log message
     * @param duration The operation duration
     * @param memoryUsage Memory usage in bytes
     * @param operationCount Number of operations performed
     * @return A new LogEntry instance
     */
    public static LogEntry forPerformance(String message, Duration duration, Long memoryUsage, Long operationCount) {
        return LogEntry.builder()
                .timestamp(Instant.now())
                .category(LogCategory.PERFORMANCE)
                .level(LogLevel.INFO)
                .message(message)
                .threadName(Thread.currentThread().getName())
                .duration(duration)
                .memoryUsage(memoryUsage)
                .operationCount(operationCount)
                .build();
    }

    /**
     * Get metadata value by key with type casting.
     *
     * @param key The metadata key
     * @param type The expected type
     * @param <T> The type parameter
     * @return The metadata value cast to the specified type, or null if not found or wrong type
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key, Class<T> type) {
        Object value = metadata.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    /**
     * Get string metadata value.
     *
     * @param key The metadata key
     * @return The metadata value as a string, or null if not found
     */
    public String getStringMetadata(String key) {
        return getMetadata(key, String.class);
    }

    /**
     * Get numeric metadata value.
     *
     * @param key The metadata key
     * @return The metadata value as a number, or null if not found
     */
    public Number getNumericMetadata(String key) {
        return getMetadata(key, Number.class);
    }

    /**
     * Get boolean metadata value.
     *
     * @param key The metadata key
     * @return The metadata value as a boolean, or null if not found
     */
    public Boolean getBooleanMetadata(String key) {
        return getMetadata(key, Boolean.class);
    }

    /**
     * Check if this log entry represents an error condition.
     *
     * @return true if this is an error entry (has error or error message)
     */
    public boolean isError() {
        return error != null || errorMessage != null || level == LogLevel.ERROR;
    }

    /**
     * Check if this log entry represents a successful action.
     *
     * @return true if this represents a successful action
     */
    public boolean isSuccessfulAction() {
        return actionType != null && Boolean.TRUE.equals(success);
    }

    /**
     * Check if this log entry represents a failed action.
     *
     * @return true if this represents a failed action
     */
    public boolean isFailedAction() {
        return actionType != null && Boolean.FALSE.equals(success);
    }

    /**
     * Check if this log entry has timing information.
     *
     * @return true if duration is present
     */
    public boolean hasTiming() {
        return duration != null;
    }

    /**
     * Check if this log entry has location information.
     *
     * @return true if location is present
     */
    public boolean hasLocation() {
        return location != null;
    }

    /**
     * Check if this log entry has correlation information.
     *
     * @return true if correlation ID is present
     */
    public boolean hasCorrelation() {
        return correlationId != null && !correlationId.trim().isEmpty();
    }

    /**
     * Get the duration in milliseconds.
     *
     * @return Duration in milliseconds, or 0 if no duration is set
     */
    public long getDurationMs() {
        return duration != null ? duration.toMillis() : 0;
    }

    /**
     * Get the duration in seconds as a formatted string.
     *
     * @return Duration formatted as "X.XXXs", or "0.000s" if no duration
     */
    public String getFormattedDuration() {
        if (duration == null) {
            return "0.000s";
        }
        double seconds = duration.toMillis() / 1000.0;
        return String.format("%.3fs", seconds);
    }

    /**
     * Get a summary description of this log entry.
     *
     * @return A brief summary suitable for debugging
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(category).append(" ");
        sb.append(level).append(": ");
        sb.append(message != null ? message : "No message");

        if (actionType != null) {
            sb.append(" [").append(actionType);
            if (actionTarget != null) {
                sb.append("->").append(actionTarget);
            }
            sb.append("]");
        }

        if (hasCorrelation()) {
            sb.append(" (").append(correlationId).append(")");
        }

        return sb.toString();
    }

    /**
     * Create a copy of this log entry with additional metadata.
     *
     * @param additionalMetadata The metadata to add
     * @return A new LogEntry with the additional metadata
     */
    public LogEntry withAdditionalMetadata(Map<String, Object> additionalMetadata) {
        Map<String, Object> newMetadata = new HashMap<>(this.metadata);
        newMetadata.putAll(additionalMetadata);
        return this.toBuilder()
                .metadata(newMetadata)
                .build();
    }

    /**
     * Create a copy of this log entry with a different level.
     *
     * @param newLevel The new log level
     * @return A new LogEntry with the updated level
     */
    public LogEntry withLevel(LogLevel newLevel) {
        return this.toBuilder()
                .level(newLevel)
                .build();
    }

    /**
     * Create a copy of this log entry with correlation information.
     *
     * @param correlationId The correlation ID
     * @param sessionId The session ID
     * @return A new LogEntry with correlation information
     */
    public LogEntry withCorrelation(String correlationId, String sessionId) {
        return this.toBuilder()
                .correlationId(correlationId)
                .sessionId(sessionId)
                .build();
    }
}