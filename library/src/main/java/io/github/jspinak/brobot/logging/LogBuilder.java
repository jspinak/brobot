package io.github.jspinak.brobot.logging;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Standalone implementation of the fluent builder API for log entries.
 *
 * <p>This class provides a concrete implementation of the LogBuilder interface
 * that can be used independently or as part of the BrobotLogger system.
 * It supports all the fluent API methods for building complex log entries
 * with rich context and metadata.
 *
 * <p>Example usage:
 * <pre>{@code
 * // Create and configure a log builder
 * LogBuilder builder = new LogBuilder(LogCategory.ACTIONS, logger)
 *     .level(LogLevel.INFO)
 *     .message("Processing user action: %s", action)
 *     .action("CLICK", "submitButton")
 *     .result(true, 0.95, location)
 *     .duration(Duration.ofMillis(25))
 *     .context("userId", "12345")
 *     .correlationId("abc-123");
 *
 * // Log the entry
 * builder.log();
 * }</pre>
 *
 * <p>The builder accumulates all the context and metadata, then creates
 * a complete LogEntry when log() is called. This allows for incremental
 * building of complex log entries with all necessary context.
 */
public class LogBuilder implements BrobotLogger.LogBuilder {

    private final LogCategory category;
    private final BrobotLogger logger;
    private LogLevel level = LogLevel.INFO;
    private String message;
    private final Map<String, Object> context = new java.util.concurrent.ConcurrentHashMap<>();
    private String actionType;
    private String actionTarget;
    private Boolean success;
    private Double similarity;
    private io.github.jspinak.brobot.model.element.Location location;
    private Duration duration;
    private String correlationId;
    private String currentState;
    private Throwable error;
    private String errorMessage;
    private Long memoryBytes;
    private Long operationCount;

    /**
     * Create a new LogBuilder for the specified category and logger.
     *
     * @param category The log category
     * @param logger The logger to use for output
     */
    public LogBuilder(LogCategory category, BrobotLogger logger) {
        this.category = category;
        this.logger = logger;
    }

    @Override
    public BrobotLogger.LogBuilder level(LogLevel level) {
        this.level = level;
        return this;
    }

    @Override
    public BrobotLogger.LogBuilder message(String message, Object... args) {
        this.message = args.length > 0 ? String.format(message, args) : message;
        return this;
    }

    @Override
    public BrobotLogger.LogBuilder context(String key, Object value) {
        this.context.put(key, value);
        return this;
    }

    @Override
    public BrobotLogger.LogBuilder context(Map<String, Object> context) {
        this.context.putAll(context);
        return this;
    }

    @Override
    public BrobotLogger.LogBuilder action(String actionType, String target) {
        this.actionType = actionType;
        this.actionTarget = target;
        return this;
    }

    @Override
    public BrobotLogger.LogBuilder result(boolean success, double similarity, io.github.jspinak.brobot.model.element.Location location) {
        this.success = success;
        this.similarity = similarity;
        this.location = location;
        return this;
    }

    @Override
    public BrobotLogger.LogBuilder duration(Duration duration) {
        this.duration = duration;
        return this;
    }

    @Override
    public BrobotLogger.LogBuilder durationSince(Instant startTime) {
        this.duration = Duration.between(startTime, Instant.now());
        return this;
    }

    @Override
    public BrobotLogger.LogBuilder correlationId(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    @Override
    public BrobotLogger.LogBuilder state(String state) {
        this.currentState = state;
        return this;
    }

    @Override
    public BrobotLogger.LogBuilder error(Throwable error) {
        this.error = error;
        this.errorMessage = error != null ? error.getMessage() : null;
        // Auto-escalate to ERROR level if not already set to ERROR or WARN
        if (this.level != LogLevel.ERROR && this.level != LogLevel.WARN) {
            this.level = LogLevel.ERROR;
        }
        return this;
    }

    @Override
    public BrobotLogger.LogBuilder error(String errorMessage) {
        this.errorMessage = errorMessage;
        // Auto-escalate to ERROR level if not already set to ERROR or WARN
        if (this.level != LogLevel.ERROR && this.level != LogLevel.WARN) {
            this.level = LogLevel.ERROR;
        }
        return this;
    }

    @Override
    public BrobotLogger.LogBuilder memory(long memoryBytes) {
        this.memoryBytes = memoryBytes;
        return this;
    }

    @Override
    public BrobotLogger.LogBuilder operationCount(long count) {
        this.operationCount = count;
        return this;
    }

    @Override
    public void log() {
        // Check if logging is enabled before building the entry
        if (!logger.isLoggingEnabled(category, level)) {
            return;
        }

        // Build the complete context map
        Map<String, Object> completeContext = new java.util.HashMap<>(context);

        // Add action context
        if (actionType != null) {
            completeContext.put("actionType", actionType);
        }
        if (actionTarget != null) {
            completeContext.put("actionTarget", actionTarget);
        }
        if (success != null) {
            completeContext.put("success", success);
        }
        if (similarity != null) {
            completeContext.put("similarity", similarity);
        }

        // Add location context
        if (location != null) {
            completeContext.put("location.x", location.getX());
            completeContext.put("location.y", location.getY());
        }

        // Add timing context
        if (duration != null) {
            completeContext.put("duration", duration.toMillis());
            completeContext.put("durationFormatted", formatDuration(duration));
        }

        // Add state context
        if (currentState != null) {
            completeContext.put("currentState", currentState);
        }

        // Add error context
        if (error != null) {
            completeContext.put("error.type", error.getClass().getSimpleName());
            completeContext.put("error.message", error.getMessage());
        }
        if (errorMessage != null) {
            completeContext.put("errorMessage", errorMessage);
        }

        // Add performance context
        if (memoryBytes != null) {
            completeContext.put("memoryUsage", memoryBytes);
            completeContext.put("memoryFormatted", formatMemory(memoryBytes));
        }
        if (operationCount != null) {
            completeContext.put("operationCount", operationCount);
        }

        // Add correlation context if explicitly set
        if (correlationId != null) {
            completeContext.put("correlationId", correlationId);
        }

        // Build the final message with action context if available
        String finalMessage = buildFinalMessage();

        // Log through the logger
        logger.log(category, level, finalMessage, completeContext);
    }

    /**
     * Build the final message with action context.
     *
     * @return The formatted message
     */
    private String buildFinalMessage() {
        if (message == null) {
            // Generate a default message based on available context
            if (actionType != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(actionType);
                if (actionTarget != null) {
                    sb.append(" on ").append(actionTarget);
                }
                if (success != null) {
                    sb.append(success ? " succeeded" : " failed");
                }
                return sb.toString();
            } else if (error != null || errorMessage != null) {
                return "Error occurred: " + (errorMessage != null ? errorMessage : error.getMessage());
            } else {
                return "Log entry";
            }
        }
        return message;
    }

    /**
     * Format duration in human-readable format.
     *
     * @param duration The duration to format
     * @return Formatted duration string
     */
    private String formatDuration(Duration duration) {
        if (duration == null) {
            return "0.000s";
        }
        double seconds = duration.toMillis() / 1000.0;
        return String.format("%.3fs", seconds);
    }

    /**
     * Format memory usage in human-readable format.
     *
     * @param bytes Memory in bytes
     * @return Formatted memory string
     */
    private String formatMemory(long bytes) {
        if (bytes < 1024) {
            return bytes + "B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1fKB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1fMB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1fGB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * Get the current log level.
     *
     * @return The current log level
     */
    public LogLevel getLevel() {
        return level;
    }

    /**
     * Get the log category.
     *
     * @return The log category
     */
    public LogCategory getCategory() {
        return category;
    }

    /**
     * Get the current message.
     *
     * @return The current message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get a copy of the current context.
     *
     * @return A copy of the context map
     */
    public Map<String, Object> getContext() {
        return new java.util.HashMap<>(context);
    }

    /**
     * Check if this builder has action context.
     *
     * @return true if action type is set
     */
    public boolean hasActionContext() {
        return actionType != null;
    }

    /**
     * Check if this builder has error context.
     *
     * @return true if error or error message is set
     */
    public boolean hasErrorContext() {
        return error != null || errorMessage != null;
    }

    /**
     * Check if this builder has timing context.
     *
     * @return true if duration is set
     */
    public boolean hasTimingContext() {
        return duration != null;
    }

    /**
     * Get a summary of the builder state for debugging.
     *
     * @return A summary string
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(category).append(" ").append(level);
        if (message != null) {
            sb.append(": ").append(message);
        }
        if (actionType != null) {
            sb.append(" [").append(actionType);
            if (actionTarget != null) {
                sb.append("->").append(actionTarget);
            }
            sb.append("]");
        }
        return sb.toString();
    }
}