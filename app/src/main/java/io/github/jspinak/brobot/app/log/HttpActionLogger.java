package io.github.jspinak.brobot.app.log;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.imageUtils.VideoRecorderService;
import io.github.jspinak.brobot.log.entities.LogEntry;
import io.github.jspinak.brobot.log.entities.PerformanceMetrics;
import io.github.jspinak.brobot.log.service.LogEntryService;
import io.github.jspinak.brobot.logging.ActionLogger;
import io.github.jspinak.brobot.logging.TestSessionLogger;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

/**
 * Implements the ActionLogger interface and creates different types of logs.
 */
@Component
public class HttpActionLogger implements ActionLogger, TestSessionLogger {

    private final LogSender logSender;
    private final LogEntryService logEntryService;
    private final VideoRecorderService videoRecorderService;

    public HttpActionLogger(LogSender logSender, LogEntryService logEntryService,
                            VideoRecorderService videoRecorderService) {
        this.logSender = logSender;
        this.logEntryService = logEntryService;
        this.videoRecorderService = videoRecorderService;
    }

    private Long getCurrentProjectId() {
        return BrobotSettings.getCurrentProjectId();
    }

    @Override
    public LogEntry logAction(String sessionId, Matches results) {
        LogEntry logEntry = new LogEntry();
        logEntry.setProjectId(getCurrentProjectId());
        logEntry.setSessionId(sessionId);
        logEntry.setType("ACTION");
        logEntry.setDescription(results.getActionDescription());
        logEntry.setTimestamp(Instant.now());
        logEntry.setActionPerformed(results.getActionOptions().getAction().toString());
        logEntry.setDuration(results.getDuration().toMillis());
        logEntry.setPassed(results.isSuccess());
        return logEntryService.saveLog(logEntry);
    }

    @Override
    public LogEntry logObservation(String sessionId, String observationType, String description, String severity) {
        LogEntry logEntry = new LogEntry();
        logEntry.setProjectId(getCurrentProjectId());
        logEntry.setSessionId(sessionId);
        logEntry.setType("OBSERVATION");
        logEntry.setDescription(String.format("%s: %s (%s)", observationType, description, severity));
        logEntry.setTimestamp(Instant.now());
        return logEntryService.saveLog(logEntry);
    }

    @Override
    public LogEntry logStateTransition(String sessionId, String fromState, String toState, boolean success, long transitionTime) {
        LogEntry logEntry = new LogEntry();
        logEntry.setProjectId(getCurrentProjectId());
        logEntry.setSessionId(sessionId);
        logEntry.setType("STATE_TRANSITION");
        logEntry.setDescription(String.format("Transition from %s to %s: %s", fromState, toState, success ? "SUCCESS" : "FAILURE"));
        logEntry.setTimestamp(Instant.now());
        logEntry.setPassed(success);
        logEntry.setDuration(transitionTime);
        return logEntryService.saveLog(logEntry);
    }

    @Override
    public LogEntry logPerformanceMetrics(String sessionId, long actionDuration, long pageLoadTime, long totalTestDuration) {
        LogEntry logEntry = new LogEntry();
        logEntry.setProjectId(getCurrentProjectId());
        logEntry.setSessionId(sessionId);
        logEntry.setType("PERFORMANCE_METRICS");
        logEntry.setTimestamp(Instant.now());

        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setActionDuration(actionDuration);
        metrics.setPageLoadTime(pageLoadTime);
        metrics.setTotalTestDuration(totalTestDuration);
        logEntry.setPerformance(metrics);

        return logEntryService.saveLog(logEntry);
    }

    @Override
    public LogEntry logError(String sessionId, String errorMessage, String screenshotPath) {
        LogEntry logEntry = new LogEntry();
        logEntry.setProjectId(getCurrentProjectId());
        logEntry.setSessionId(sessionId);
        logEntry.setType("ERROR");
        logEntry.setDescription(errorMessage);
        logEntry.setTimestamp(Instant.now());
        logEntry.setPassed(false);
        logEntry.setScreenshotPath(screenshotPath);
        return logEntryService.saveLog(logEntry);
    }

    @Override
    public LogEntry startVideoRecording(String sessionId) throws IOException, AWTException {
        String fileName = "recordings/" + sessionId + "_" + System.currentTimeMillis() + ".avi";
        videoRecorderService.startRecording(fileName);

        LogEntry logEntry = new LogEntry();
        logEntry.setProjectId(getCurrentProjectId());
        logEntry.setSessionId(sessionId);
        logEntry.setType("VIDEO_RECORDING_START");
        logEntry.setDescription("Started video recording: " + fileName);
        logEntry.setTimestamp(Instant.now());
        return logEntryService.saveLog(logEntry);
    }

    @Override
    public LogEntry stopVideoRecording(String sessionId) throws IOException {
        videoRecorderService.stopRecording();

        LogEntry logEntry = new LogEntry();
        logEntry.setProjectId(getCurrentProjectId());
        logEntry.setSessionId(sessionId);
        logEntry.setType("VIDEO_RECORDING_STOP");
        logEntry.setDescription("Stopped video recording");
        logEntry.setTimestamp(Instant.now());
        return logEntryService.saveLog(logEntry);
    }

    @Override
    public String startSession(String applicationUnderTest) {
        String sessionId = UUID.randomUUID().toString();
        LogEntry logEntry = new LogEntry(sessionId, "SESSION_START", "Started session for " + applicationUnderTest);
        sendLogToClient(logEntry);
        return sessionId;
    }

    @Override
    public void endSession(String sessionId) {
        LogEntry logEntry = new LogEntry(sessionId, "SESSION_END", "Ended session");
        sendLogToClient(logEntry);
    }

    @Override
    public void setCurrentState(String sessionId, String stateName, String stateDescription) {
        String description = String.format("Current State: %s - %s", stateName, stateDescription);
        LogEntry logEntry = new LogEntry(sessionId, "CURRENT_STATE", description);
        sendLogToClient(logEntry);
    }

    private void sendLogToClient(LogEntry logEntry) {
        try {
            logSender.sendLog(logEntry);
        } catch (Exception e) {
            // Handle exception (e.g., log to local file, retry mechanism, etc.)
            System.err.println("Failed to send log to client app: " + e.getMessage());
        }
    }
}
