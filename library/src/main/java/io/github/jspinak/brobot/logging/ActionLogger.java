package io.github.jspinak.brobot.logging;

import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.log.entities.LogEntry;

import java.awt.*;
import java.io.IOException;

/**
 * Interface for creating different types of logs.
 */
public interface ActionLogger {
    default LogEntry logAction(String sessionId, Matches results, ObjectCollection objectCollection) {
        // No-op implementation
        return null;
    }

    default LogEntry logObservation(String sessionId, String observationType, String description, String severity) {
        // No-op implementation
        return null;
    }

    default LogEntry logStateTransition(String sessionId, String fromState, String toState, boolean success, long transitionTime) {
        // No-op implementation
        return null;
    }

    default LogEntry logPerformanceMetrics(String sessionId, long actionDuration, long pageLoadTime, long totalTestDuration) {
        // No-op implementation
        return null;
    }

    default LogEntry logError(String sessionId, String errorMessage, String screenshotPath) {
        // No-op implementation
        return null;
    }

    default LogEntry startVideoRecording(String sessionId) throws IOException, AWTException {
        // No-op implementation
        return null;
    }

    default LogEntry stopVideoRecording(String sessionId) throws IOException {
        // No-op implementation
        return null;
    }
}