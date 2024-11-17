package io.github.jspinak.brobot.app.log;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.imageUtils.VideoRecorderService;
import io.github.jspinak.brobot.log.entities.*;
import io.github.jspinak.brobot.log.service.LogEntryService;
import io.github.jspinak.brobot.logging.ActionLogger;
import io.github.jspinak.brobot.logging.TestSessionLogger;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Implements the ActionLogger interface and creates different types of logs.
 */
@Component
public class HttpActionLogger implements ActionLogger, TestSessionLogger {

    private final LogSender logSender;
    private final LogEntryService logEntryService;
    private final VideoRecorderService videoRecorderService;
    private final LogEntryStateImageMapper logEntryStateImageMapper;

    public HttpActionLogger(LogSender logSender, LogEntryService logEntryService,
            VideoRecorderService videoRecorderService, LogEntryStateImageMapper logEntryStateImageMapper) {
        this.logSender = logSender;
        this.logEntryService = logEntryService;
        this.videoRecorderService = videoRecorderService;
        this.logEntryStateImageMapper = logEntryStateImageMapper;
    }

    private Long getCurrentProjectId() {
        return BrobotSettings.getCurrentProjectId();
    }

    @Override
    public LogEntry logAction(String sessionId, Matches matches, ObjectCollection objectCollection) {
        LogEntry logEntry = new LogEntry();
        logEntry.setProjectId(getCurrentProjectId());
        logEntry.setSessionId(sessionId);
        logEntry.setType(LogType.ACTION);
        logEntry.setActionType(matches.getActionOptions().getAction().toString());
        logEntry.setDescription(getStateImageDescription(objectCollection, matches)); // matches.getActionDescription());
        logEntry.setTimestamp(Instant.now());
        logEntry.setActionPerformed(matches.getActionOptions().getAction().toString());
        logEntry.setDuration(matches.getDuration().toMillis());
        logEntry.setSuccess(matches.isSuccess());
        logEntry.setCurrentStateName(getStateInFocus(objectCollection));
        objectCollection.getStateImages().forEach(
                sI -> logEntry.getStateImages().add(logEntryStateImageMapper.toLog(sI, matches)));
        return logEntryService.saveLog(logEntry);
    }

    private String getStateImageDescription(ObjectCollection objectCollection, Matches matches) {
        List<StateImage> stateImages = objectCollection.getStateImages();
        if (stateImages.isEmpty())
            return "";
        StringBuilder stringBuilder = new StringBuilder();
        for (StateImage stateImage : stateImages) {
            stringBuilder.append(stateImage.getName()).append(" ");
        }
        return stringBuilder.toString();
    }

    private String getStateInFocus(ObjectCollection objectCollection) {
        List<StateImage> stateImages = objectCollection.getStateImages();
        if (stateImages.isEmpty())
            return "";
        for (StateImage stateImage : stateImages) {
            if (!stateImage.getOwnerStateName().isEmpty())
                return stateImage.getOwnerStateName();
        }
        return "";
    }

    @Override
    public LogEntry logStateTransition(String sessionId, State fromState, Set<State> toStates,
                                       Set<State> beforeStates, boolean success, long transitionTime) {
        LogEntry logEntry = new LogEntry();
        logEntry.setSessionId(sessionId);
        logEntry.setProjectId(getCurrentProjectId());
        logEntry.setFromStateName(fromState.getName());
        logEntry.setFromStateId(fromState.getId());
        beforeStates.forEach(state -> {
            logEntry.getBeforeStateNames().add(state.getName());
            logEntry.getBeforeStateIds().add(state.getId());
        });
        toStates.forEach(state -> {
            logEntry.getToStateNames().add(state.getName());
            logEntry.getToStateIds().add(state.getId());
        });
        if (transitionTime > 0) {
            logEntry.setDuration(transitionTime);
            logEntry.setSuccess(success);
        }
        return logEntryService.saveLog(logEntry);
    }

    @Override
    public LogEntry logPerformanceMetrics(String sessionId, long actionDuration, long pageLoadTime,
                                                                                long totalTestDuration) {
        LogEntry logEntry = new LogEntry();
        logEntry.setProjectId(getCurrentProjectId());
        logEntry.setSessionId(sessionId);
        logEntry.setType(LogType.METRICS);
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
        logEntry.setType(LogType.ERROR);
        logEntry.setDescription(errorMessage);
        logEntry.setTimestamp(Instant.now());
        logEntry.setSuccess(false);
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
        logEntry.setType(LogType.VIDEO);
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
        logEntry.setType(LogType.VIDEO);
        logEntry.setDescription("Stopped video recording");
        logEntry.setTimestamp(Instant.now());
        return logEntryService.saveLog(logEntry);
    }

    @Override
    public String startSession(String applicationUnderTest) {
        String sessionId = UUID.randomUUID().toString();
        LogEntry logEntry = new LogEntry();
        logEntry.setType(LogType.SESSION);
        logEntry.setDescription("Started session for " + applicationUnderTest);
        sendLogToClient(logEntry);
        return sessionId;
    }

    @Override
    public void endSession(String sessionId) {
        LogEntry logEntry = new LogEntry(sessionId, LogType.SESSION, "Ended session");
        sendLogToClient(logEntry);
    }

    @Override
    public void setCurrentState(String sessionId, String stateName, String stateDescription) {
        String description = String.format("Current State: %s - %s", stateName, stateDescription);
        LogEntry logEntry = new LogEntry(sessionId, LogType.SESSION, description);
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