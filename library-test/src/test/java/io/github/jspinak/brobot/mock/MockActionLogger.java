package io.github.jspinak.brobot.mock;

import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.report.log.model.LogData;
import io.github.jspinak.brobot.report.log.model.LogType;
import io.github.jspinak.brobot.report.log.ActionLogger;
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
    public LogData logAction(String sessionId, Matches results, ObjectCollection objectCollection) {
        System.out.println("Mock logAction: SessionId=" + sessionId + ", Results=" + results);
        return createMockLogEntry(sessionId, LogType.ACTION);
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
        return createMockLogEntry(sessionId, LogType.TRANSITION);
    }

    @Override
    public LogData logPerformanceMetrics(String sessionId, long actionDuration, long pageLoadTime,
                                         long totalTestDuration) {
        System.out.println("Mock logPerformanceMetrics: SessionId=" + sessionId +
                ", ActionDuration=" + actionDuration +
                ", PageLoadTime=" + pageLoadTime +
                ", TotalTestDuration=" + totalTestDuration);
        return createMockLogEntry(sessionId, LogType.METRICS);
    }

    @Override
    public LogData logError(String sessionId, String errorMessage, String screenshotPath) {
        System.out.println("Mock logError: SessionId=" + sessionId +
                ", ErrorMessage=" + errorMessage +
                ", ScreenshotPath=" + screenshotPath);
        return createMockLogEntry(sessionId, LogType.ERROR);
    }

    @Override
    public LogData startVideoRecording(String sessionId) throws IOException, AWTException {
        System.out.println("Mock startVideoRecording: SessionId=" + sessionId);
        return createMockLogEntry(sessionId, LogType.VIDEO);
    }

    @Override
    public LogData stopVideoRecording(String sessionId) throws IOException {
        System.out.println("Mock stopVideoRecording: SessionId=" + sessionId);
        return createMockLogEntry(sessionId, LogType.VIDEO);
    }

    private LogData createMockLogEntry(String sessionId, LogType type) {
        LogData logData = new LogData(sessionId, type, "Mock log entry");
        logData.setTimestamp(Instant.now());
        return logData;
    }
}