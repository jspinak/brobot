package io.github.jspinak.brobot.mock;

import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.log.entities.LogEntry;
import io.github.jspinak.brobot.log.entities.LogType;
import io.github.jspinak.brobot.logging.ActionLogger;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.Set;

@Component
@Primary
public class MockActionLogger implements ActionLogger {

    @Override
    public LogEntry logAction(String sessionId, Matches results, ObjectCollection objectCollection) {
        System.out.println("Mock logAction: SessionId=" + sessionId + ", Results=" + results);
        return createMockLogEntry(sessionId, LogType.ACTION);
    }

    @Override
    public LogEntry logStateTransition(String sessionId, State fromState, Set<State> toStates,
            Set<State> beforeStates, boolean success, long transitionTime) {
        System.out.println("Mock logStateTransition: SessionId=" + sessionId +
                ", From State=" + fromState.getName() +
                ", To States=[" + String.join(", ", toStates.stream().map(State::getName).toList()) + "]" +
                ", Success=" + success +
                ", Time=" + transitionTime + "ms");
        return createMockLogEntry(sessionId, LogType.TRANSITION);
    }

    @Override
    public LogEntry logPerformanceMetrics(String sessionId, long actionDuration, long pageLoadTime,
            long totalTestDuration) {
        System.out.println("Mock logPerformanceMetrics: SessionId=" + sessionId +
                ", ActionDuration=" + actionDuration +
                ", PageLoadTime=" + pageLoadTime +
                ", TotalTestDuration=" + totalTestDuration);
        return createMockLogEntry(sessionId, LogType.METRICS);
    }

    @Override
    public LogEntry logError(String sessionId, String errorMessage, String screenshotPath) {
        System.out.println("Mock logError: SessionId=" + sessionId +
                ", ErrorMessage=" + errorMessage +
                ", ScreenshotPath=" + screenshotPath);
        return createMockLogEntry(sessionId, LogType.ERROR);
    }

    @Override
    public LogEntry startVideoRecording(String sessionId) throws IOException, AWTException {
        System.out.println("Mock startVideoRecording: SessionId=" + sessionId);
        return createMockLogEntry(sessionId, LogType.VIDEO);
    }

    @Override
    public LogEntry stopVideoRecording(String sessionId) throws IOException {
        System.out.println("Mock stopVideoRecording: SessionId=" + sessionId);
        return createMockLogEntry(sessionId, LogType.VIDEO);
    }

    private LogEntry createMockLogEntry(String sessionId, LogType type) {
        LogEntry logEntry = new LogEntry(sessionId, type, "Mock log entry");
        logEntry.setTimestamp(Instant.now());
        return logEntry;
    }
}