package io.github.jspinak.brobot.logging.unified;

import java.util.HashMap;
import java.util.Map;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.state.StateObject;

/**
 * Fluent builder for creating complex log entries.
 *
 * <p>LogBuilder provides a fluent API for constructing detailed log entries with custom metadata,
 * performance metrics, and contextual information. It's designed for scenarios where the simple
 * logging methods in BrobotLogger are insufficient.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * logger.log()
 *     .level(Level.INFO)
 *     .action("CLICK")
 *     .target(loginButton)
 *     .result(actionResult)
 *     .screenshot("/tmp/screenshot.png")
 *     .metadata("retries", 3)
 *     .performance("responseTime", 250)
 *     .color(AnsiColor.GREEN)
 *     .log();
 * }</pre>
 *
 * <p>The builder automatically includes context from LoggingContext (session ID, current state,
 * etc.) and validates the constructed event before logging.
 *
 * @since 2.0
 * @see BrobotLogger
 * @see LogEvent
 */
public class LogBuilder {

    private final BrobotLogger logger;
    private final LoggingContext context;
    private final LogEvent.Builder eventBuilder;
    private final Map<String, Object> metadata = new HashMap<>();
    private String[] colors = new String[0];

    /**
     * Creates a new LogBuilder instance. Package-private as it should only be created by
     * BrobotLogger.
     *
     * @param logger The parent logger instance
     * @param context The logging context for automatic context inclusion
     */
    LogBuilder(BrobotLogger logger, LoggingContext context) {
        this.logger = logger;
        this.context = context;
        this.eventBuilder = LogEvent.builder();

        // Set default values
        eventBuilder.type(LogEvent.Type.ACTION);
        eventBuilder.level(LogEvent.Level.INFO);

        // Include context
        if (context.getSessionId() != null) {
            eventBuilder.sessionId(context.getSessionId());
        }
        if (context.getCurrentState() != null) {
            eventBuilder.stateId(context.getCurrentState().getName());
        }
    }

    /**
     * Sets the log level.
     *
     * @param level The log level (DEBUG, INFO, WARNING, ERROR)
     * @return This builder for chaining
     */
    public LogBuilder level(LogEvent.Level level) {
        eventBuilder.level(level);
        return this;
    }

    /**
     * Sets the action type.
     *
     * @param action The action performed (e.g., "CLICK", "TYPE", "HOVER")
     * @return This builder for chaining
     */
    public LogBuilder action(String action) {
        eventBuilder.action(action);
        return this;
    }

    /**
     * Sets the target state object.
     *
     * @param object The state object that was the target of the action
     * @return This builder for chaining
     */
    public LogBuilder target(StateObject object) {
        if (object != null) {
            eventBuilder.target(object.getName());
        }
        return this;
    }

    /**
     * Sets the target by name.
     *
     * @param targetName The name of the target
     * @return This builder for chaining
     */
    public LogBuilder target(String targetName) {
        eventBuilder.target(targetName);
        return this;
    }

    /**
     * Sets the action result.
     *
     * @param result The result of the action execution
     * @return This builder for chaining
     */
    public LogBuilder result(ActionResult result) {
        if (result != null) {
            eventBuilder.success(result.isSuccess());
            if (result.getDuration() != null) {
                eventBuilder.duration(result.getDuration().toMillis());
            }
            metadata.put("matchCount", result.getMatchList().size());
            if (!result.getText().isEmpty()) {
                metadata.put("extractedText", result.getText());
            }
        }
        return this;
    }

    /**
     * Sets a screenshot path.
     *
     * @param path The path to the screenshot
     * @return This builder for chaining
     */
    public LogBuilder screenshot(String path) {
        metadata.put("screenshot", path);
        return this;
    }

    /**
     * Adds a single metadata entry.
     *
     * @param key The metadata key
     * @param value The metadata value
     * @return This builder for chaining
     */
    public LogBuilder metadata(String key, Object value) {
        metadata.put(key, value);
        return this;
    }

    /**
     * Adds multiple metadata entries.
     *
     * @param metadata Map of metadata to add
     * @return This builder for chaining
     */
    public LogBuilder metadata(Map<String, Object> metadata) {
        if (metadata != null) {
            this.metadata.putAll(metadata);
        }
        return this;
    }

    /**
     * Adds a performance metric.
     *
     * @param metric The metric name
     * @param value The metric value (typically milliseconds)
     * @return This builder for chaining
     */
    public LogBuilder performance(String metric, long value) {
        metadata.put("perf_" + metric, value);
        return this;
    }

    /**
     * Sets console output colors.
     *
     * @param colors ANSI color codes for console output
     * @return This builder for chaining
     */
    public LogBuilder color(String... colors) {
        this.colors = colors;
        return this;
    }

    /**
     * Sets the log message.
     *
     * @param message The log message
     * @return This builder for chaining
     */
    public LogBuilder message(String message) {
        eventBuilder.message(message);
        return this;
    }

    /**
     * Sets the event type.
     *
     * @param type The event type
     * @return This builder for chaining
     */
    public LogBuilder type(LogEvent.Type type) {
        eventBuilder.type(type);
        return this;
    }

    /**
     * Sets success status.
     *
     * @param success Whether the operation was successful
     * @return This builder for chaining
     */
    public LogBuilder success(boolean success) {
        eventBuilder.success(success);
        return this;
    }

    /**
     * Sets the duration.
     *
     * @param millis Duration in milliseconds
     * @return This builder for chaining
     */
    public LogBuilder duration(long millis) {
        eventBuilder.duration(millis);
        return this;
    }

    /**
     * Sets an error/exception.
     *
     * @param error The exception that occurred
     * @return This builder for chaining
     */
    public LogBuilder error(Throwable error) {
        eventBuilder.error(error);
        eventBuilder.type(LogEvent.Type.ERROR);
        eventBuilder.level(LogEvent.Level.ERROR);
        return this;
    }

    /**
     * Sets state transition information.
     *
     * @param fromState The state transitioning from
     * @param toState The state transitioning to
     * @return This builder for chaining
     */
    public LogBuilder transition(String fromState, String toState) {
        eventBuilder.type(LogEvent.Type.TRANSITION);
        eventBuilder.fromState(fromState);
        eventBuilder.toState(toState);
        return this;
    }

    /**
     * Marks this as an observation.
     *
     * @param observation The observation message
     * @return This builder for chaining
     */
    public LogBuilder observation(String observation) {
        eventBuilder.type(LogEvent.Type.OBSERVATION);
        eventBuilder.message(observation);
        return this;
    }

    /**
     * Marks this as a performance log.
     *
     * @return This builder for chaining
     */
    public LogBuilder performanceLog() {
        eventBuilder.type(LogEvent.Type.PERFORMANCE);
        return this;
    }

    /**
     * Builds and logs the event.
     *
     * <p>This method constructs the final LogEvent, adds all metadata, and routes it through the
     * logger's message router. The event is logged to all configured handlers based on the routing
     * rules.
     */
    public void log() {
        // Add all metadata to the event
        if (!metadata.isEmpty()) {
            eventBuilder.metadata(metadata);
        }

        // Add context metadata if available
        Map<String, Object> contextMetadata = context.getAllMetadata();
        if (!contextMetadata.isEmpty()) {
            eventBuilder.metadata(contextMetadata);
        }

        // Set timestamp if not already set
        eventBuilder.timestamp(System.currentTimeMillis());

        // Build and route the event
        LogEvent event = eventBuilder.build();

        // If custom colors were specified, we need special handling
        if (colors.length > 0) {
            // Store colors in metadata for the router to use
            Map<String, Object> colorMetadata = new HashMap<>();
            colorMetadata.put("_consoleColors", colors);

            // Combine existing metadata with color metadata
            Map<String, Object> combinedMetadata = new HashMap<>();
            if (event.getMetadata() != null) {
                combinedMetadata.putAll(event.getMetadata());
            }
            combinedMetadata.putAll(colorMetadata);

            LogEvent.Builder rebuilder =
                    LogEvent.builder()
                            .type(event.getType())
                            .level(event.getLevel())
                            .message(event.getMessage())
                            .timestamp(event.getTimestamp())
                            .sessionId(event.getSessionId())
                            .stateId(event.getStateId())
                            .action(event.getAction())
                            .target(event.getTarget())
                            .fromState(event.getFromState())
                            .toState(event.getToState())
                            .success(event.isSuccess())
                            .error(event.getError())
                            .metadata(combinedMetadata);

            // Only set duration if not null
            if (event.getDuration() != null) {
                rebuilder.duration(event.getDuration());
            }

            event = rebuilder.build();
        }

        logger.routeEvent(event);
    }
}
