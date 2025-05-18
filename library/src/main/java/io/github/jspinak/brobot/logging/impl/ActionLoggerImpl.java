package io.github.jspinak.brobot.logging.impl;

import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.log.entities.LogEntry;
import io.github.jspinak.brobot.log.entities.LogType;
import io.github.jspinak.brobot.logging.ActionLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Component
@Qualifier("actionLoggerImpl")
public class ActionLoggerImpl implements ActionLogger {
    private static final Logger logger = LoggerFactory.getLogger(ActionLoggerImpl.class);

    @Override
    public LogEntry logAction(String sessionId, Matches results, ObjectCollection objectCollection) {
        String description = results != null ? results.getOutputText() : "No results data";
        boolean success = results != null && results.isSuccess();

        LogEntry entry = createLogEntry(sessionId, LogType.ACTION, description, success);
        logger.info("Action logged: {} - {}", sessionId, description);
        return entry;
    }

    @Override
    public LogEntry logStateTransition(String sessionId, Set<State> fromStates, Set<State> toStates,
                                       Set<State> beforeStates, boolean success, long transitionTime) {
        String description = "Transition from " + formatStateSet(fromStates) +
                " to " + formatStateSet(toStates) +
                " (" + transitionTime + "ms)";

        LogEntry entry = createLogEntry(sessionId, LogType.TRANSITION, description, success);
        logger.info("State transition logged: {} - {}", sessionId, description);
        return entry;
    }

    @Override
    public LogEntry logObservation(String sessionId, String observationType, String description, String severity) {
        LogEntry entry = createLogEntry(sessionId, LogType.OBSERVATION,
                observationType + ": " + description, true);
        logger.info("Observation logged: {} - {}: {}", sessionId, observationType, description);
        return entry;
    }

    @Override
    public LogEntry logPerformanceMetrics(String sessionId, long actionDuration,
                                          long pageLoadTime, long totalTestDuration) {
        String description = "Performance metrics - Action: " + actionDuration +
                "ms, Page load: " + pageLoadTime +
                "ms, Total: " + totalTestDuration + "ms";

        LogEntry entry = createLogEntry(sessionId, LogType.METRICS, description, true);
        logger.info("Performance metrics logged: {} - {}", sessionId, description);
        return entry;
    }

    @Override
    public LogEntry logError(String sessionId, String errorMessage, String screenshotPath) {
        String description = errorMessage;
        if (screenshotPath != null) {
            description += " (Screenshot: " + screenshotPath + ")";
        }

        LogEntry entry = createLogEntry(sessionId, LogType.ERROR, description, false);
        logger.error("Error logged: {} - {}", sessionId, errorMessage);
        return entry;
    }

    @Override
    public LogEntry startVideoRecording(String sessionId) throws IOException, AWTException {
        LogEntry entry = createLogEntry(sessionId, LogType.VIDEO, "Started video recording", true);
        logger.info("Video recording started: {}", sessionId);
        return entry;
    }

    @Override
    public LogEntry stopVideoRecording(String sessionId) throws IOException {
        LogEntry entry = createLogEntry(sessionId, LogType.VIDEO, "Stopped video recording", true);
        logger.info("Video recording stopped: {}", sessionId);
        return entry;
    }

    private LogEntry createLogEntry(String sessionId, LogType type, String description, boolean success) {
        LogEntry entry = new LogEntry();
        entry.setId(Long.valueOf(UUID.randomUUID().toString()));
        entry.setSessionId(sessionId);
        entry.setTimestamp(Instant.from(LocalDateTime.now()));
        entry.setType(type);
        entry.setDescription(description);
        entry.setSuccess(success);
        return entry;
    }

    private String formatStateSet(Set<State> states) {
        if (states == null || states.isEmpty()) {
            return "None";
        }

        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (State state : states) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(state.getName());
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }
}