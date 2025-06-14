package io.github.jspinak.brobot.report.log.impl;

import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.report.log.model.LogData;
import io.github.jspinak.brobot.report.log.model.LogType;
import io.github.jspinak.brobot.report.log.ActionLogger;
import io.github.jspinak.brobot.report.log.spi.LogSink;
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
    private final LogSink logSink;

    public ActionLoggerImpl(LogSink logSink) {
        this.logSink = logSink;
    }

    @Override
    public LogData logAction(String sessionId, Matches results, ObjectCollection objectCollection) {
        String description = results != null ? results.getOutputText() : "No results data";
        boolean success = results != null && results.isSuccess();

        LogData entry = createLogEntry(sessionId, LogType.ACTION, description, success);
        logger.info("Action logged: {} - {}", sessionId, description);
        return entry;
    }

    @Override
    public LogData logStateTransition(String sessionId, Set<State> fromStates, Set<State> toStates,
                                      Set<State> beforeStates, boolean success, long transitionTime) {
        String description = "Transition from " + formatStateSet(fromStates) +
                " to " + formatStateSet(toStates) +
                " (" + transitionTime + "ms)";

        LogData logData = createLogEntry(sessionId, LogType.TRANSITION, description, success);
        logger.info("State transition logged: {} - {}", sessionId, description);
        return logData;
    }

    @Override
    public LogData logObservation(String sessionId, String observationType, String description, String severity) {
        LogData entry = createLogEntry(sessionId, LogType.OBSERVATION,
                observationType + ": " + description, true);
        logger.info("Observation logged: {} - {}: {}", sessionId, observationType, description);
        return entry;
    }

    @Override
    public LogData logPerformanceMetrics(String sessionId, long actionDuration,
                                         long pageLoadTime, long totalTestDuration) {
        String description = "Performance metrics - Action: " + actionDuration +
                "ms, Page load: " + pageLoadTime +
                "ms, Total: " + totalTestDuration + "ms";

        LogData entry = createLogEntry(sessionId, LogType.METRICS, description, true);
        logger.info("Performance metrics logged: {} - {}", sessionId, description);
        return entry;
    }

    @Override
    public LogData logError(String sessionId, String errorMessage, String screenshotPath) {
        String description = errorMessage;
        if (screenshotPath != null) {
            description += " (Screenshot: " + screenshotPath + ")";
        }

        LogData entry = createLogEntry(sessionId, LogType.ERROR, description, false);
        logger.error("Error logged: {} - {}", sessionId, errorMessage);
        return entry;
    }

    @Override
    public LogData startVideoRecording(String sessionId) throws IOException, AWTException {
        LogData entry = createLogEntry(sessionId, LogType.VIDEO, "Started video recording", true);
        logger.info("Video recording started: {}", sessionId);
        return entry;
    }

    @Override
    public LogData stopVideoRecording(String sessionId) throws IOException {
        LogData entry = createLogEntry(sessionId, LogType.VIDEO, "Stopped video recording", true);
        logger.info("Video recording stopped: {}", sessionId);
        return entry;
    }

    private LogData createLogEntry(String sessionId, LogType type, String description, boolean success) {
        LogData entry = new LogData();
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