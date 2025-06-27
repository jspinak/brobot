package io.github.jspinak.brobot.mock;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.tools.logging.ActionLogger;
import io.github.jspinak.brobot.tools.logging.model.LogData;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;

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
    public LogData logAction(String sessionId, ActionResult results, ObjectCollection objectCollection) {
        System.out.println("Mock logAction: SessionId=" + sessionId + ", Results=" + results);
        return createMockLogEntry(sessionId, LogEventType.ACTION);
    }

    @Override
    public LogData logStateTransition(String sessionId, Set<State> fromStates, Set<State> toStates,
                                      Set<State> beforeStates, boolean success, long transitionTime) {
        String fromStatesString = fromStates.stream()
                .map(State::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("None");
        System.out.println("Mock logStateTransition: SessionId=" + sessionId +
                ", From State=" + fromStatesString +
                ", To States=[" + String.join(", ", toStates.stream().map(State::getName).toList()) + "]" +
                ", Success=" + success +
                ", Time=" + transitionTime + "ms");
        return createMockLogEntry(sessionId, LogEventType.TRANSITION);
    }

    @Override
    public LogData logPerformanceMetrics(String sessionId, long actionDuration, long pageLoadTime,
                                         long totalTestDuration) {
        System.out.println("Mock logPerformanceMetrics: SessionId=" + sessionId +
                ", ActionDuration=" + actionDuration +
                ", PageLoadTime=" + pageLoadTime +
                ", TotalTestDuration=" + totalTestDuration);
        return createMockLogEntry(sessionId, LogEventType.METRICS);
    }

    @Override
    public LogData logError(String sessionId, String errorMessage, String screenshotPath) {
        System.out.println("Mock logError: SessionId=" + sessionId +
                ", ErrorMessage=" + errorMessage +
                ", ScreenshotPath=" + screenshotPath);
        return createMockLogEntry(sessionId, LogEventType.ERROR);
    }

    @Override
    public LogData startVideoRecording(String sessionId) throws IOException, AWTException {
        System.out.println("Mock startVideoRecording: SessionId=" + sessionId);
        return createMockLogEntry(sessionId, LogEventType.VIDEO);
    }

    @Override
    public LogData stopVideoRecording(String sessionId) throws IOException {
        System.out.println("Mock stopVideoRecording: SessionId=" + sessionId);
        return createMockLogEntry(sessionId, LogEventType.VIDEO);
    }

    private LogData createMockLogEntry(String sessionId, LogEventType type) {
        LogData logData = new LogData(sessionId, type, "Mock log entry");
        logData.setTimestamp(Instant.now());
        return logData;
    }
}