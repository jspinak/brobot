package io.github.jspinak.brobot.runner.events;

import java.awt.*;
import java.io.IOException;
import java.util.Set;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.State;
// Removed old logging import: import io.github.jspinak.brobot.tools.logging.ActionLogger;
import io.github.jspinak.brobot.tools.logging.SessionLifecycleLogger;
import io.github.jspinak.brobot.tools.logging.model.LogData;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;

import lombok.Data;

/**
 * Decorator that implements both ActionLogger and TestSessionLogger interfaces, adding event
 * publishing behavior to all logging operations.
 */
@Data
public class EventPublishingActionLogger implements ActionLogger, SessionLifecycleLogger {
    private final ActionLogger actionLogger;
    private final SessionLifecycleLogger sessionLogger;
    private final EventBus eventBus;

    public EventPublishingActionLogger(
            ActionLogger actionLogger, SessionLifecycleLogger sessionLogger, EventBus eventBus) {
        this.actionLogger = actionLogger;
        this.sessionLogger = sessionLogger;
        this.eventBus = eventBus;
    }

    //
    // ActionLogger implementation
    //

    @Override
    public LogData logAction(
            String sessionId, ActionResult matches, ObjectCollection objectCollection) {
        LogData logData = actionLogger.logAction(sessionId, matches, objectCollection);
        publishLogEntryEvent(logData);

        // For failed actions, also publish an error event
        if (!matches.isSuccess()) {
            publishErrorForFailedAction(matches, logData);
        }

        return logData;
    }

    @Override
    public LogData logStateTransition(
            String sessionId,
            Set<State> fromStates,
            Set<State> toStates,
            Set<State> beforeStates,
            boolean success,
            long transitionTime) {
        LogData logData =
                actionLogger.logStateTransition(
                        sessionId, fromStates, toStates, beforeStates, success, transitionTime);
        publishLogEntryEvent(logData);

        // For failed transitions, also publish an error event
        if (!success) {
            ErrorEvent errorEvent =
                    ErrorEvent.medium(
                            this,
                            "State transition failed from "
                                    + getStateSetNames(fromStates)
                                    + " to "
                                    + getStateSetNames(toStates),
                            null,
                            "StateTransitionManager");
            eventBus.publish(errorEvent);
        }

        return logData;
    }

    @Override
    public LogData logObservation(
            String sessionId, String observationType, String description, String severity) {
        LogData logData =
                actionLogger.logObservation(sessionId, observationType, description, severity);
        publishLogEntryEvent(logData);
        return logData;
    }

    @Override
    public LogData logPerformanceMetrics(
            String sessionId, long actionDuration, long pageLoadTime, long totalTestDuration) {
        LogData logData =
                actionLogger.logPerformanceMetrics(
                        sessionId, actionDuration, pageLoadTime, totalTestDuration);
        publishLogEntryEvent(logData);
        return logData;
    }

    @Override
    public LogData logError(String sessionId, String errorMessage, String screenshotPath) {
        LogData logData = actionLogger.logError(sessionId, errorMessage, screenshotPath);
        publishLogEntryEvent(logData);

        // Also publish an error event
        ErrorEvent errorEvent = ErrorEvent.high(this, errorMessage, null, "Automation");
        eventBus.publish(errorEvent);

        return logData;
    }

    @Override
    public LogData startVideoRecording(String sessionId) throws IOException, AWTException {
        LogData logData = actionLogger.startVideoRecording(sessionId);
        publishLogEntryEvent(logData);
        return logData;
    }

    @Override
    public LogData stopVideoRecording(String sessionId) throws IOException {
        LogData logData = actionLogger.stopVideoRecording(sessionId);
        publishLogEntryEvent(logData);
        return logData;
    }

    //
    // TestSessionLogger implementation
    //

    @Override
    public String startSession(String applicationUnderTest) {
        String sessionId = sessionLogger.startSession(applicationUnderTest);

        // Publish session start event
        LogEvent logEvent =
                LogEvent.info(
                        this,
                        "Started session for " + applicationUnderTest + " with ID " + sessionId,
                        "Session");
        eventBus.publish(logEvent);

        return sessionId;
    }

    @Override
    public void endSession(String sessionId) {
        sessionLogger.endSession(sessionId);

        // Publish session end event
        LogEvent logEvent = LogEvent.info(this, "Ended session with ID " + sessionId, "Session");
        eventBus.publish(logEvent);
    }

    @Override
    public void setCurrentState(String sessionId, String stateName, String stateDescription) {
        sessionLogger.setCurrentState(sessionId, stateName, stateDescription);

        // Publish state change event
        LogEvent logEvent =
                LogEvent.info(
                        this,
                        "State changed to: " + stateName + " - " + stateDescription,
                        "StateManagement");
        eventBus.publish(logEvent);
    }

    //
    // Helper methods
    //

    private void publishLogEntryEvent(LogData logData) {
        // Skip if logEntry is null (which can happen with no-op implementations)
        if (logData == null) return;

        LogEntryEvent event = LogEntryEvent.created(this, logData);
        eventBus.publish(event);

        // Also publish a corresponding log event for better integration
        LogEvent.LogLevel level = getLogLevelForEntry(logData);
        LogEvent logEvent =
                new LogEvent(
                        BrobotEvent.EventType.LOG_MESSAGE,
                        this,
                        logData.getDescription(),
                        level,
                        logData.getType() != null ? logData.getType().toString() : "UNKNOWN",
                        null);
        eventBus.publish(logEvent);
    }

    private void publishErrorForFailedAction(ActionResult matches, LogData logData) {
        ErrorEvent errorEvent =
                ErrorEvent.medium(
                        this,
                        "Action failed: "
                                + matches.getActionConfig().getClass().getSimpleName()
                                + " - "
                                + matches.getOutputText(),
                        null,
                        "ActionExecution");
        eventBus.publish(errorEvent);
    }

    private LogEvent.LogLevel getLogLevelForEntry(LogData logData) {
        if (logData.getType() == LogEventType.ERROR) {
            return LogEvent.LogLevel.ERROR;
        } else if (!logData.isSuccess()
                && (logData.getType() == LogEventType.ACTION
                        || logData.getType() == LogEventType.TRANSITION)) {
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
