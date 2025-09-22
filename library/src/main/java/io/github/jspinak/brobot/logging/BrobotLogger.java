package io.github.jspinak.brobot.logging;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import io.github.jspinak.brobot.logging.events.ActionEvent;
import io.github.jspinak.brobot.logging.events.MatchEvent;
import io.github.jspinak.brobot.logging.events.PerformanceEvent;
import io.github.jspinak.brobot.logging.events.TransitionEvent;

/**
 * Core logger interface for the Brobot framework.
 *
 * <p>Provides structured logging capabilities with support for different event types, categories,
 * and levels. The interface supports both high-level event logging and flexible fluent API for
 * building custom log entries.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Structured event logging for actions, transitions, matches, and performance
 *   <li>Category-based filtering and configuration
 *   <li>Fluent API for building complex log entries
 *   <li>Integration with correlation tracking
 *   <li>Support for custom metadata and context
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // Event-based logging
 * logger.logAction(ActionEvent.success("CLICK", "submitButton", Duration.ofMillis(25)));
 *
 * // Fluent API
 * logger.builder(LogCategory.ACTIONS)
 *     .level(LogLevel.INFO)
 *     .message("Clicking submit button")
 *     .action("CLICK", "submitButton")
 *     .result(true, 0.95, new Location(100, 200))
 *     .duration(Duration.ofMillis(25))
 *     .log();
 * }</pre>
 */
public interface BrobotLogger {

    /**
     * Log an action event with all action-specific context.
     *
     * @param event The action event to log
     */
    void logAction(ActionEvent event);

    /**
     * Log a state transition event.
     *
     * @param event The transition event to log
     */
    void logTransition(TransitionEvent event);

    /**
     * Log a pattern matching event with match results.
     *
     * @param event The match event to log
     */
    void logMatch(MatchEvent event);

    /**
     * Log performance metrics and timing information.
     *
     * @param event The performance event to log
     */
    void logPerformance(PerformanceEvent event);

    /**
     * Generic structured logging with custom context.
     *
     * @param category The log category
     * @param level The log level
     * @param message The log message
     * @param context Additional context data
     */
    void log(LogCategory category, LogLevel level, String message, Map<String, Object> context);

    /**
     * Simple logging with category and level.
     *
     * @param category The log category
     * @param level The log level
     * @param message The log message
     */
    default void log(LogCategory category, LogLevel level, String message) {
        log(category, level, message, java.util.Collections.emptyMap());
    }

    /**
     * Check if logging is enabled for the given category and level.
     *
     * @param category The log category
     * @param level The log level
     * @return true if logging is enabled for this category/level combination
     */
    boolean isLoggingEnabled(LogCategory category, LogLevel level);

    /**
     * Create a fluent builder for constructing complex log entries.
     *
     * @param category The log category
     * @return A new LogBuilder instance
     */
    LogBuilder builder(LogCategory category);

    /**
     * Fluent API for building structured log entries.
     *
     * <p>Provides a convenient way to build log entries with context, timing, and metadata. All
     * methods return the builder for chaining.
     */
    interface LogBuilder {

        /**
         * Set the log level.
         *
         * @param level The log level
         * @return this builder for chaining
         */
        LogBuilder level(LogLevel level);

        /**
         * Set the log message with optional formatting.
         *
         * @param message The message template
         * @param args Arguments for String.format
         * @return this builder for chaining
         */
        LogBuilder message(String message, Object... args);

        /**
         * Add a context key-value pair.
         *
         * @param key The context key
         * @param value The context value
         * @return this builder for chaining
         */
        LogBuilder context(String key, Object value);

        /**
         * Add multiple context entries.
         *
         * @param context Map of context entries
         * @return this builder for chaining
         */
        LogBuilder context(Map<String, Object> context);

        /**
         * Add action-specific context.
         *
         * @param actionType The type of action
         * @param target The action target
         * @return this builder for chaining
         */
        LogBuilder action(String actionType, String target);

        /**
         * Add result context (success, similarity, location).
         *
         * @param success Whether the action succeeded
         * @param similarity Similarity score (if applicable)
         * @param location Location of the action (if applicable)
         * @return this builder for chaining
         */
        LogBuilder result(
                boolean success,
                double similarity,
                io.github.jspinak.brobot.model.element.Location location);

        /**
         * Add timing information.
         *
         * @param duration The operation duration
         * @return this builder for chaining
         */
        LogBuilder duration(Duration duration);

        /**
         * Add timing information from start time.
         *
         * @param startTime The start time
         * @return this builder for chaining
         */
        LogBuilder durationSince(Instant startTime);

        /**
         * Add correlation ID for request tracking.
         *
         * @param correlationId The correlation ID
         * @return this builder for chaining
         */
        LogBuilder correlationId(String correlationId);

        /**
         * Add current state context.
         *
         * @param state The current state name
         * @return this builder for chaining
         */
        LogBuilder state(String state);

        /**
         * Add error information.
         *
         * @param error The error/exception
         * @return this builder for chaining
         */
        LogBuilder error(Throwable error);

        /**
         * Add error message.
         *
         * @param errorMessage The error message
         * @return this builder for chaining
         */
        LogBuilder error(String errorMessage);

        /**
         * Add memory usage information.
         *
         * @param memoryBytes Memory used in bytes
         * @return this builder for chaining
         */
        LogBuilder memory(long memoryBytes);

        /**
         * Add operation count.
         *
         * @param count Number of operations performed
         * @return this builder for chaining
         */
        LogBuilder operationCount(long count);

        /** Finalize and emit the log entry. */
        void log();
    }

    /** Convenience methods for common logging patterns. */

    /**
     * Log an info message.
     *
     * @param category The log category
     * @param message The message
     */
    default void info(LogCategory category, String message) {
        log(category, LogLevel.INFO, message);
    }

    /**
     * Log a debug message.
     *
     * @param category The log category
     * @param message The message
     */
    default void debug(LogCategory category, String message) {
        log(category, LogLevel.DEBUG, message);
    }

    /**
     * Log a warning message.
     *
     * @param category The log category
     * @param message The message
     */
    default void warn(LogCategory category, String message) {
        log(category, LogLevel.WARN, message);
    }

    /**
     * Log an error message.
     *
     * @param category The log category
     * @param message The message
     */
    default void error(LogCategory category, String message) {
        log(category, LogLevel.ERROR, message);
    }

    /**
     * Log a trace message.
     *
     * @param category The log category
     * @param message The message
     */
    default void trace(LogCategory category, String message) {
        log(category, LogLevel.TRACE, message);
    }
}
