package io.github.jspinak.brobot.logging.unified;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.config.logging.LoggingVerbosityConfig;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Unified logging facade for the Brobot framework.
 * 
 * <p>BrobotLogger provides a single entry point for all logging needs in the framework,
 * consolidating the functionality of SLF4J, ActionLogger, and ConsoleReporter into a
 * cohesive API. This design simplifies the developer experience while providing powerful
 * capabilities for debugging, monitoring, and analysis.</p>
 * 
 * <p>Key features:
 * <ul>
 *   <li>Simple methods for common logging scenarios (80% use case)</li>
 *   <li>Fluent builder API for complex logging needs</li>
 *   <li>Automatic context propagation (session, state, action)</li>
 *   <li>Performance tracking utilities</li>
 *   <li>Configurable output levels and formats</li>
 * </ul>
 * </p>
 * 
 * <p>Usage examples:
 * <pre>{@code
 * // Simple action logging
 * brobotLogger.action("CLICK", loginButton);
 * 
 * // Complex logging with builder
 * brobotLogger.log()
 *     .action(CLICK)
 *     .target(loginButton)
 *     .result(actionResult)
 *     .performance("responseTime", 250)
 *     .log();
 * 
 * // Session scoped logging
 * try (var session = brobotLogger.session("test-123")) {
 *     brobotLogger.action("START", testCase);
 *     // ... test execution ...
 * }
 * }</pre>
 * </p>
 * 
 * @since 2.0
 * @see LogBuilder
 * @see LoggingContext
 * @see LogEvent
 */
@Component
public class BrobotLogger {
    
    private final LoggingContext context;
    private final MessageRouter router;
    private LoggingVerbosityConfig verbosityConfig;
    private ConsoleReporter.OutputLevel consoleLevel = ConsoleReporter.OutputLevel.HIGH;
    private boolean structuredLoggingEnabled = false;

    public BrobotLogger(LoggingContext context, MessageRouter router) {
        this.context = context;
        this.router = router;
    }
    
    @Autowired(required = false)
    public void setVerbosityConfig(LoggingVerbosityConfig verbosityConfig) {
        this.verbosityConfig = verbosityConfig;
    }

    /**
     * Logs an action performed on a state object.
     * 
     * <p>This method provides the simplest way to log automation actions. It automatically
     * captures the current context (session, state) and routes the log entry to appropriate
     * handlers based on configuration.</p>
     * 
     * @param action The action performed (e.g., "CLICK", "TYPE", "HOVER")
     * @param target The state object that was the target of the action
     */
    public void action(String action, StateObject target) {
        LogEvent event = LogEvent.builder()
                .type(LogEvent.Type.ACTION)
                .action(action)
                .target(target.getName())
                .sessionId(context.getSessionId())
                .stateId(context.getCurrentState() != null ? context.getCurrentState().getName() : null)
                .timestamp(System.currentTimeMillis())
                .build();
        
        router.route(event);
    }

    /**
     * Logs an action with its result.
     * 
     * @param action The action performed
     * @param target The state object that was the target
     * @param result The result of the action execution
     */
    public void action(String action, StateObject target, ActionResult result) {
        LogEvent.Builder eventBuilder = LogEvent.builder()
                .type(LogEvent.Type.ACTION)
                .action(action)
                .target(target.getName())
                .success(result.isSuccess())
                .sessionId(context.getSessionId())
                .stateId(context.getCurrentState() != null ? context.getCurrentState().getName() : null)
                .timestamp(System.currentTimeMillis())
                .metadata("matchCount", result.getMatchList().size());
        
        // Add duration if available
        if (result.getDuration() != null) {
            eventBuilder.duration(result.getDuration().toMillis());
        }
        
        // Add match coordinates if available
        if (!result.getMatchList().isEmpty()) {
            // Get the first (best) match
            Object firstMatch = result.getMatchList().get(0);
            if (firstMatch instanceof Match) {
                Match match = (Match) firstMatch;
                eventBuilder.metadata("matchX", match.x())
                           .metadata("matchY", match.y());
            }
        }
        
        router.route(eventBuilder.build());
    }

    /**
     * Logs a state transition.
     * 
     * @param from The state transitioning from
     * @param to The state transitioning to
     */
    public void transition(State from, State to) {
        transition(from, to, true, 0);
    }

    /**
     * Logs a state transition with success status and duration.
     * 
     * @param from The state transitioning from
     * @param to The state transitioning to
     * @param success Whether the transition was successful
     * @param duration The duration of the transition in milliseconds
     */
    public void transition(State from, State to, boolean success, long duration) {
        LogEvent event = LogEvent.builder()
                .type(LogEvent.Type.TRANSITION)
                .fromState(from != null ? from.getName() : "UNKNOWN")
                .toState(to != null ? to.getName() : "UNKNOWN")
                .success(success)
                .duration(duration)
                .sessionId(context.getSessionId())
                .timestamp(System.currentTimeMillis())
                .build();
        
        router.route(event);
    }

    /**
     * Logs an observation about the application state.
     * 
     * @param observation The observation message
     */
    public void observation(String observation) {
        observation(observation, "INFO");
    }

    /**
     * Logs an observation with severity level.
     * 
     * @param observation The observation message
     * @param severity The severity level (e.g., "INFO", "WARNING", "ERROR")
     */
    public void observation(String observation, String severity) {
        LogEvent event = LogEvent.builder()
                .type(LogEvent.Type.OBSERVATION)
                .message(observation)
                .level(LogEvent.Level.valueOf(severity))
                .sessionId(context.getSessionId())
                .stateId(context.getCurrentState() != null ? context.getCurrentState().getName() : null)
                .timestamp(System.currentTimeMillis())
                .build();
        
        router.route(event);
    }

    /**
     * Logs an error.
     * 
     * @param message The error message
     * @param cause The exception that caused the error (may be null)
     */
    public void error(String message, Throwable cause) {
        LogEvent event = LogEvent.builder()
                .type(LogEvent.Type.ERROR)
                .message(message)
                .level(LogEvent.Level.ERROR)
                .error(cause)
                .sessionId(context.getSessionId())
                .stateId(context.getCurrentState() != null ? context.getCurrentState().getName() : null)
                .timestamp(System.currentTimeMillis())
                .success(false)
                .build();
        
        router.route(event);
    }

    /**
     * Creates a fluent log builder for complex logging scenarios.
     * 
     * <p>The builder pattern allows for more detailed log entries with custom metadata,
     * performance metrics, screenshots, and other contextual information.</p>
     * 
     * @return A new log builder instance
     */
    public LogBuilder log() {
        return new LogBuilder(this, context);
    }

    /**
     * Creates a session scope for correlated logging.
     * 
     * <p>All logs within the session scope will automatically include the session ID
     * for correlation. The session is automatically closed when used with try-with-resources.</p>
     * 
     * <pre>{@code
     * try (var session = logger.session("test-123")) {
     *     // All logs here will include session ID "test-123"
     * }
     * }</pre>
     * 
     * @param sessionId The unique session identifier
     * @return An AutoCloseable that will clear the session when closed
     */
    public AutoCloseable session(String sessionId) {
        context.setSessionId(sessionId);
        return () -> context.clearSession();
    }

    /**
     * Creates an operation scope for detailed performance tracking.
     * 
     * <p>Operations are automatically timed and logged when the scope closes.</p>
     * 
     * @param operationName The name of the operation
     * @return An AutoCloseable that will log the operation duration when closed
     */
    public AutoCloseable operation(String operationName) {
        long startTime = System.currentTimeMillis();
        context.pushOperation(operationName);
        
        return () -> {
            long duration = System.currentTimeMillis() - startTime;
            context.popOperation();
            
            LogEvent event = LogEvent.builder()
                    .type(LogEvent.Type.PERFORMANCE)
                    .message("Operation completed: " + operationName)
                    .duration(duration)
                    .sessionId(context.getSessionId())
                    .timestamp(System.currentTimeMillis())
                    .metadata("operation", operationName)
                    .build();
            
            router.route(event);
        };
    }

    /**
     * Starts a named timer for performance measurement.
     * 
     * @param timerName The name of the timer
     * @return A Timer instance that can be stopped to log the duration
     */
    public Timer startTimer(String timerName) {
        return new Timer(timerName, this);
    }

    /**
     * Sets the console output level.
     * 
     * @param level The desired output level
     */
    public void setConsoleLevel(ConsoleReporter.OutputLevel level) {
        this.consoleLevel = level;
        ConsoleReporter.outputLevel = level;
    }

    /**
     * Enables or disables structured logging output.
     * 
     * @param enable true to enable structured logging, false to disable
     */
    public void enableStructuredLogging(boolean enable) {
        this.structuredLoggingEnabled = enable;
        router.setStructuredLoggingEnabled(enable);
    }

    /**
     * Gets the current console output level.
     * 
     * @return The current output level
     */
    public ConsoleReporter.OutputLevel getConsoleLevel() {
        return consoleLevel;
    }

    /**
     * Checks if structured logging is enabled.
     * 
     * @return true if structured logging is enabled
     */
    public boolean isStructuredLoggingEnabled() {
        return structuredLoggingEnabled;
    }
    
    /**
     * Sets the logging verbosity level.
     * 
     * @param level The desired verbosity level (NORMAL or VERBOSE)
     */
    public void setVerbosity(LoggingVerbosityConfig.VerbosityLevel level) {
        if (verbosityConfig != null) {
            verbosityConfig.setVerbosity(level);
        }
    }
    
    /**
     * Gets the current logging verbosity level.
     * 
     * @return The current verbosity level
     */
    public LoggingVerbosityConfig.VerbosityLevel getVerbosity() {
        if (verbosityConfig != null) {
            return verbosityConfig.getVerbosity();
        }
        return LoggingVerbosityConfig.VerbosityLevel.NORMAL;
    }

    /**
     * Routes a log event through the message router.
     * Package-private to allow LogBuilder access.
     * 
     * @param event The log event to route
     */
    void routeEvent(LogEvent event) {
        router.route(event);
    }

    /**
     * Timer utility for measuring durations.
     */
    public static class Timer implements AutoCloseable {
        private final String name;
        private final long startTime;
        private final BrobotLogger logger;

        Timer(String name, BrobotLogger logger) {
            this.name = name;
            this.startTime = System.currentTimeMillis();
            this.logger = logger;
        }

        /**
         * Stops the timer and logs the duration.
         * 
         * @return The duration in milliseconds
         */
        public long stop() {
            long duration = System.currentTimeMillis() - startTime;
            
            LogEvent event = LogEvent.builder()
                    .type(LogEvent.Type.PERFORMANCE)
                    .message("Timer stopped: " + name)
                    .duration(duration)
                    .sessionId(logger.context.getSessionId())
                    .timestamp(System.currentTimeMillis())
                    .metadata("timer", name)
                    .build();
            
            logger.routeEvent(event);
            return duration;
        }

        @Override
        public void close() {
            stop();
        }
    }
}