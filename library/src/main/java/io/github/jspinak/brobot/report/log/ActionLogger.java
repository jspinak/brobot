package io.github.jspinak.brobot.report.log;

import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.report.log.model.LogData;

import java.awt.*;
import java.io.IOException;
import java.util.Set;

/**
 * Interface for creating different types of logs.
 */
public interface ActionLogger {
    default LogData logAction(String sessionId, Matches results, ObjectCollection objectCollection) {
        // No-op implementation
        return null;
    }

    default LogData logObservation(String sessionId, String observationType, String description, String severity) {
        // No-op implementation
        return null;
    }

    default LogData logStateTransition(String sessionId, Set<State> fromStates, Set<State> toStates,
                                       Set<State> beforeStates, boolean success, long transitionTime) {
        // No-op implementation
        return null;
    }

    default LogData logPerformanceMetrics(String sessionId, long actionDuration, long pageLoadTime, long totalTestDuration) {
        // No-op implementation
        return null;
    }

    default LogData logError(String sessionId, String errorMessage, String screenshotPath) {
        // No-op implementation
        return null;
    }

    default LogData startVideoRecording(String sessionId) throws IOException, AWTException {
        // No-op implementation
        return null;
    }

    default LogData stopVideoRecording(String sessionId) throws IOException {
        // No-op implementation
        return null;
    }
}