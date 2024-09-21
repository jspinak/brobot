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
    LogEntry logAction(String sessionId, Matches results, ObjectCollection objectCollection);
    LogEntry logObservation(String sessionId, String observationType, String description, String severity);
    LogEntry logStateTransition(String sessionId, String fromState, String toState, boolean success, long transitionTime);
    LogEntry logPerformanceMetrics(String sessionId, long actionDuration, long pageLoadTime, long totalTestDuration);
    LogEntry logError(String sessionId, String errorMessage, String screenshotPath);
    LogEntry startVideoRecording(String sessionId) throws IOException, AWTException;
    LogEntry stopVideoRecording(String sessionId) throws IOException;
}