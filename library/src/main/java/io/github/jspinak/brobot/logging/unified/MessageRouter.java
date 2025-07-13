package io.github.jspinak.brobot.logging.unified;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.tools.logging.ActionLogger;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.tools.logging.ansi.AnsiColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;

import java.util.HashSet;
import java.util.Set;

/**
 * Routes log events to appropriate handlers based on configuration and event type.
 * 
 * <p>MessageRouter is the central dispatching mechanism in the unified logging system.
 * It receives LogEvent instances and routes them to the appropriate existing logging
 * systems (SLF4J, ActionLogger, ConsoleReporter) based on the event type and current
 * configuration.</p>
 * 
 * <p>This component acts as an adapter layer during the migration period, allowing
 * the new unified API to work with existing logging infrastructure. Over time,
 * the routing logic can be enhanced to support new handlers and sinks.</p>
 * 
 * <p>Routing rules:
 * <ul>
 *   <li>All events go to SLF4J for traditional logging</li>
 *   <li>ACTION events go to ActionLogger for structured persistence</li>
 *   <li>All events go to ConsoleReporter based on output level</li>
 *   <li>Structured logging can be enabled/disabled dynamically</li>
 * </ul>
 * </p>
 * 
 * @since 2.0
 * @see LogEvent
 * @see BrobotLogger
 */
@Component
public class MessageRouter {
    
    private static final Logger slf4jLogger = LoggerFactory.getLogger(BrobotLogger.class);
    
    private final ActionLogger actionLogger;
    private boolean structuredLoggingEnabled = false;
    
    @Autowired
    public MessageRouter(ActionLogger actionLogger) {
        this.actionLogger = actionLogger;
    }
    
    /**
     * Routes a log event to appropriate handlers.
     * 
     * @param event The log event to route
     */
    public void route(LogEvent event) {
        // Route to SLF4J
        routeToSlf4j(event);
        
        // Route to ConsoleReporter
        routeToConsole(event);
        
        // Route to ActionLogger if structured logging is enabled
        if (structuredLoggingEnabled) {
            routeToActionLogger(event);
        }
    }
    
    /**
     * Routes event to SLF4J logger.
     */
    private void routeToSlf4j(LogEvent event) {
        String message = formatSlf4jMessage(event);
        
        switch (event.getLevel()) {
            case DEBUG:
                slf4jLogger.debug(message);
                break;
            case INFO:
                slf4jLogger.info(message);
                break;
            case WARNING:
                slf4jLogger.warn(message);
                break;
            case ERROR:
                if (event.getError() != null) {
                    slf4jLogger.error(message, event.getError());
                } else {
                    slf4jLogger.error(message);
                }
                break;
        }
    }
    
    /**
     * Routes event to ConsoleReporter for immediate console output.
     */
    private void routeToConsole(LogEvent event) {
        ConsoleReporter.OutputLevel level = determineConsoleLevel(event);
        
        if (!ConsoleReporter.minReportingLevel(level)) {
            return;
        }
        
        String output = event.toFormattedString();
        String[] colors = determineColors(event);
        
        if (colors.length > 0) {
            ConsoleReporter.println(output, colors);
        } else {
            ConsoleReporter.println(level, output);
        }
    }
    
    /**
     * Routes event to ActionLogger for structured persistence.
     */
    private void routeToActionLogger(LogEvent event) {
        if (event.getSessionId() == null) {
            return; // ActionLogger requires session ID
        }
        
        switch (event.getType()) {
            case ACTION:
                logAction(event);
                break;
                
            case TRANSITION:
                logTransition(event);
                break;
                
            case OBSERVATION:
                actionLogger.logObservation(
                    event.getSessionId(),
                    "OBSERVATION",
                    event.getMessage() != null ? event.getMessage() : "",
                    event.getLevel().toString()
                );
                break;
                
            case PERFORMANCE:
                if (event.getDuration() != null) {
                    actionLogger.logPerformanceMetrics(
                        event.getSessionId(),
                        event.getDuration(),
                        0, // Page load time not tracked in LogEvent
                        event.getDuration() // Use duration as total for now
                    );
                }
                break;
                
            case ERROR:
                actionLogger.logError(
                    event.getSessionId(),
                    event.getMessage() != null ? event.getMessage() : "Unknown error",
                    null // Screenshot path not tracked in LogEvent
                );
                break;
        }
    }
    
    /**
     * Logs an action event to ActionLogger.
     */
    private void logAction(LogEvent event) {
        // Create a minimal ActionResult for compatibility
        ActionResult result = new ActionResult();
        result.setSuccess(event.isSuccess());
        if (event.getDuration() != null) {
            result.setDuration(Duration.ofMillis(event.getDuration()));
        }
        result.setOutputText(formatActionDescription(event));
        
        // Create empty ObjectCollection (we don't have the actual objects)
        ObjectCollection collection = new ObjectCollection.Builder().build();
        
        actionLogger.logAction(event.getSessionId(), result, collection);
    }
    
    /**
     * Logs a transition event to ActionLogger.
     */
    private void logTransition(LogEvent event) {
        Set<State> fromStates = new HashSet<>();
        Set<State> toStates = new HashSet<>();
        
        // Create minimal State objects for compatibility
        if (event.getFromState() != null) {
            State fromState = new State();
            fromState.setName(event.getFromState());
            fromStates.add(fromState);
        }
        
        if (event.getToState() != null) {
            State toState = new State();
            toState.setName(event.getToState());
            toStates.add(toState);
        }
        
        long duration = event.getDuration() != null ? event.getDuration() : 0;
        
        actionLogger.logStateTransition(
            event.getSessionId(),
            fromStates,
            toStates,
            fromStates, // Use same as beforeStates for simplicity
            event.isSuccess(),
            duration
        );
    }
    
    /**
     * Formats message for SLF4J output.
     */
    private String formatSlf4jMessage(LogEvent event) {
        StringBuilder sb = new StringBuilder();
        
        if (event.getSessionId() != null) {
            sb.append("[").append(event.getSessionId()).append("] ");
        }
        
        switch (event.getType()) {
            case ACTION:
                sb.append("Action: ").append(event.getAction());
                if (event.getTarget() != null) {
                    sb.append(" on ").append(event.getTarget());
                }
                break;
                
            case TRANSITION:
                sb.append("Transition: ").append(event.getFromState())
                  .append(" -> ").append(event.getToState());
                break;
                
            case OBSERVATION:
            case PERFORMANCE:
            case ERROR:
                if (event.getMessage() != null) {
                    sb.append(event.getMessage());
                }
                break;
        }
        
        if (event.getDuration() != null) {
            sb.append(" (").append(event.getDuration()).append("ms)");
        }
        
        if (!event.isSuccess() && event.getType() != LogEvent.Type.ERROR) {
            sb.append(" [FAILED]");
        }
        
        return sb.toString();
    }
    
    /**
     * Formats action description for ActionLogger.
     */
    private String formatActionDescription(LogEvent event) {
        StringBuilder sb = new StringBuilder();
        
        if (event.getAction() != null) {
            sb.append(event.getAction());
        }
        
        if (event.getTarget() != null) {
            sb.append(" on ").append(event.getTarget());
        }
        
        if (event.getMessage() != null && !event.getMessage().isEmpty()) {
            if (sb.length() > 0) sb.append(": ");
            sb.append(event.getMessage());
        }
        
        return sb.toString();
    }
    
    /**
     * Determines the appropriate ConsoleReporter output level for an event.
     */
    private ConsoleReporter.OutputLevel determineConsoleLevel(LogEvent event) {
        // Errors always show at LOW level
        if (event.getType() == LogEvent.Type.ERROR || event.getLevel() == LogEvent.Level.ERROR) {
            return ConsoleReporter.OutputLevel.LOW;
        }
        
        // Actions and transitions show at LOW level
        if (event.getType() == LogEvent.Type.ACTION || event.getType() == LogEvent.Type.TRANSITION) {
            return ConsoleReporter.OutputLevel.LOW;
        }
        
        // Everything else requires HIGH level
        return ConsoleReporter.OutputLevel.HIGH;
    }
    
    /**
     * Determines ANSI colors based on event type and status.
     */
    private String[] determineColors(LogEvent event) {
        if (event.getType() == LogEvent.Type.ERROR || event.getLevel() == LogEvent.Level.ERROR) {
            return new String[] { AnsiColor.RED };
        }
        
        if (!event.isSuccess()) {
            return new String[] { AnsiColor.YELLOW };
        }
        
        if (event.getType() == LogEvent.Type.TRANSITION) {
            return new String[] { AnsiColor.BLUE };
        }
        
        if (event.getType() == LogEvent.Type.PERFORMANCE) {
            return new String[] { AnsiColor.CYAN };
        }
        
        return new String[0]; // No special color
    }
    
    /**
     * Enables or disables structured logging to ActionLogger.
     * 
     * @param enabled true to enable, false to disable
     */
    public void setStructuredLoggingEnabled(boolean enabled) {
        this.structuredLoggingEnabled = enabled;
    }
    
    /**
     * Checks if structured logging is enabled.
     * 
     * @return true if structured logging is enabled
     */
    public boolean isStructuredLoggingEnabled() {
        return structuredLoggingEnabled;
    }
}