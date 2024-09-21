package io.github.jspinak.brobot.test.mock;

import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.log.entities.LogEntry;
import io.github.jspinak.brobot.logging.ActionLogger;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.time.Instant;

@Component
@Primary
public class MockActionLogger implements ActionLogger {

    @Override
    public LogEntry logAction(String sessionId, Matches results, ObjectCollection objectCollection) {
        System.out.println("Mock logAction: SessionId=" + sessionId + ", Results=" + results);
        return createMockLogEntry(sessionId, "ACTION");
    }

    @Override
    public LogEntry logObservation(String sessionId, String observationType, String description, String severity) {
        System.out.println("Mock logObservation: SessionId=" + sessionId +
                ", Type=" + observationType +
                ", Description=" + description +
                ", Severity=" + severity);
        return createMockLogEntry(sessionId, "OBSERVATION");
    }

    @Override
    public LogEntry logStateTransition(String sessionId, String fromState, String toState, boolean success, long transitionTime) {
        System.out.println("Mock logStateTransition: SessionId=" + sessionId +
                ", From=" + fromState +
                ", To=" + toState +
                ", Success=" + success +
                ", Time=" + transitionTime);
        return createMockLogEntry(sessionId, "STATE_TRANSITION");
    }

    @Override
    public LogEntry logPerformanceMetrics(String sessionId, long actionDuration, long pageLoadTime, long totalTestDuration) {
        System.out.println("Mock logPerformanceMetrics: SessionId=" + sessionId +
                ", ActionDuration=" + actionDuration +
                ", PageLoadTime=" + pageLoadTime +
                ", TotalTestDuration=" + totalTestDuration);
        return createMockLogEntry(sessionId, "PERFORMANCE_METRICS");
    }

    @Override
    public LogEntry logError(String sessionId, String errorMessage, String screenshotPath) {
        System.out.println("Mock logError: SessionId=" + sessionId +
                ", ErrorMessage=" + errorMessage +
                ", ScreenshotPath=" + screenshotPath);
        return createMockLogEntry(sessionId, "ERROR");
    }

    @Override
    public LogEntry startVideoRecording(String sessionId) throws IOException, AWTException {
        System.out.println("Mock startVideoRecording: SessionId=" + sessionId);
        return createMockLogEntry(sessionId, "START_VIDEO_RECORDING");
    }

    @Override
    public LogEntry stopVideoRecording(String sessionId) throws IOException {
        System.out.println("Mock stopVideoRecording: SessionId=" + sessionId);
        return createMockLogEntry(sessionId, "STOP_VIDEO_RECORDING");
    }

    private LogEntry createMockLogEntry(String sessionId, String type) {
        LogEntry logEntry = new LogEntry(sessionId, type, "Mock log entry");
        logEntry.setTimestamp(Instant.now());
        return logEntry;
    }
}