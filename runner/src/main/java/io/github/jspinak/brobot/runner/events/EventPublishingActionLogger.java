package io.github.jspinak.brobot.runner.events;

import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.log.entities.LogEntry;
import io.github.jspinak.brobot.log.entities.LogType;
import io.github.jspinak.brobot.logging.ActionLogger;
import io.github.jspinak.brobot.logging.TestSessionLogger;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.util.Set;

/**
 * Decorator that implements both ActionLogger and TestSessionLogger interfaces,
 * adding event publishing behavior to all logging operations.
 */
@Component
@Primary
public class EventPublishingActionLogger implements ActionLogger, TestSessionLogger {
    private final ActionLogger actionLogger;
    private final TestSessionLogger sessionLogger;
    private final EventBus eventBus;

    public EventPublishingActionLogger(ActionLogger actionLogger,
                                       TestSessionLogger sessionLogger,
                                       EventBus eventBus) {
        this.actionLogger = actionLogger;
        this.sessionLogger = sessionLogger;
        this.eventBus = eventBus;
    }

    //
    // ActionLogger implementation
    //

    @Override
    public LogEntry logAction(String sessionId, Matches matches, ObjectCollection objectCollection) {
        LogEntry logEntry = actionLogger.logAction(sessionId, matches, objectCollection);
        publishLogEntryEvent(logEntry);

        // For failed actions, also publish an error event
        if (!matches.isSuccess()) {
            publishErrorForFailedAction(matches, logEntry);
        }

        return logEntry;
    }

    @Override
    public LogEntry logStateTransition(String sessionId, Set<State> fromStates,
                                       Set<State> toStates,
                                       Set<State> beforeStates,
                                       boolean success, long transitionTime) {
        LogEntry logEntry = actionLogger.logStateTransition(
                sessionId, fromStates, toStates, beforeStates, success, transitionTime);
        publishLogEntryEvent(logEntry);

        // For failed transitions, also publish an error event
        if (!success) {
            ErrorEvent errorEvent = ErrorEvent.medium(
                    this,
                    "State transition failed from " + getStateSetNames(fromStates) +
                            " to " + getStateSetNames(toStates),
                    null,
                    "StateTransitionManager"
            );
            eventBus.publish(errorEvent);
        }

        return logEntry;
    }

    @Override
    public LogEntry logObservation(String sessionId, String observationType,
                                   String description, String severity) {
        LogEntry logEntry = actionLogger.logObservation(
                sessionId, observationType, description, severity);
        publishLogEntryEvent(logEntry);
        return logEntry;
    }

    @Override
    public LogEntry logPerformanceMetrics(String sessionId, long actionDuration,
                                          long pageLoadTime, long totalTestDuration) {
        LogEntry logEntry = actionLogger.logPerformanceMetrics(
                sessionId, actionDuration, pageLoadTime, totalTestDuration);
        publishLogEntryEvent(logEntry);
        return logEntry;
    }

    @Override
    public LogEntry logError(String sessionId, String errorMessage, String screenshotPath) {
        LogEntry logEntry = actionLogger.logError(sessionId, errorMessage, screenshotPath);
        publishLogEntryEvent(logEntry);

        // Also publish an error event
        ErrorEvent errorEvent = ErrorEvent.high(
                this,
                errorMessage,
                null,
                "Automation"
        );
        eventBus.publish(errorEvent);

        return logEntry;
    }

    @Override
    public LogEntry startVideoRecording(String sessionId) throws IOException, AWTException {
        LogEntry logEntry = actionLogger.startVideoRecording(sessionId);
        publishLogEntryEvent(logEntry);
        return logEntry;
    }

    @Override
    public LogEntry stopVideoRecording(String sessionId) throws IOException {
        LogEntry logEntry = actionLogger.stopVideoRecording(sessionId);
        publishLogEntryEvent(logEntry);
        return logEntry;
    }

    //
    // TestSessionLogger implementation
    //

    @Override
    public String startSession(String applicationUnderTest) {
        String sessionId = sessionLogger.startSession(applicationUnderTest);

        // Publish session start event
        LogEvent logEvent = LogEvent.info(
                this,
                "Started session for " + applicationUnderTest + " with ID " + sessionId,
                "Session"
        );
        eventBus.publish(logEvent);

        return sessionId;
    }

    @Override
    public void endSession(String sessionId) {
        sessionLogger.endSession(sessionId);

        // Publish session end event
        LogEvent logEvent = LogEvent.info(
                this,
                "Ended session with ID " + sessionId,
                "Session"
        );
        eventBus.publish(logEvent);
    }

    @Override
    public void setCurrentState(String sessionId, String stateName, String stateDescription) {
        sessionLogger.setCurrentState(sessionId, stateName, stateDescription);

        // Publish state change event
        LogEvent logEvent = LogEvent.info(
                this,
                "State changed to: " + stateName + " - " + stateDescription,
                "StateManagement"
        );
        eventBus.publish(logEvent);
    }

    //
    // Helper methods
    //

    private void publishLogEntryEvent(LogEntry logEntry) {
        // Skip if logEntry is null (which can happen with no-op implementations)
        if (logEntry == null) return;

        LogEntryEvent event = LogEntryEvent.created(this, logEntry);
        eventBus.publish(event);

        // Also publish a corresponding log event for better integration
        LogEvent.LogLevel level = getLogLevelForEntry(logEntry);
        LogEvent logEvent = new LogEvent(
                BrobotEvent.EventType.LOG_MESSAGE,
                this,
                logEntry.getDescription(),
                level,
                logEntry.getType() != null ? logEntry.getType().toString() : "UNKNOWN",
                null
        );
        eventBus.publish(logEvent);
    }

    private void publishErrorForFailedAction(Matches matches, LogEntry logEntry) {
        ErrorEvent errorEvent = ErrorEvent.medium(
                this,
                "Action failed: " + matches.getActionOptions().getAction().toString() +
                        " - " + matches.getOutputText(),
                null,
                "ActionExecution"
        );
        eventBus.publish(errorEvent);
    }

    private LogEvent.LogLevel getLogLevelForEntry(LogEntry logEntry) {
        if (logEntry.getType() == LogType.ERROR) {
            return LogEvent.LogLevel.ERROR;
        } else if (!logEntry.isSuccess() &&
                (logEntry.getType() == LogType.ACTION || logEntry.getType() == LogType.TRANSITION)) {
            return LogEvent.LogLevel.WARNING;
        } else {
            return LogEvent.LogLevel.INFO;
        }
    }

    private String getStateSetNames(Set<State> states) {
        if (states == null || states.isEmpty()) {
            return "None";
        }

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (State state : states) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(state.getName());
            first = false;
        }
        return sb.toString();
    }
}