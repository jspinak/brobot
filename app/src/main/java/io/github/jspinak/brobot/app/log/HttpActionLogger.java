package io.github.jspinak.brobot.app.log;

import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.project.Project;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.libraryfeatures.recording.VideoRecorderService;
import io.github.jspinak.brobot.report.log.model.LogData;
import io.github.jspinak.brobot.report.log.model.LogType;
import io.github.jspinak.brobot.report.log.model.PerformanceMetricsData;
import io.github.jspinak.brobot.report.log.service.LogEntryService;
import io.github.jspinak.brobot.report.log.ActionLogger;
import io.github.jspinak.brobot.report.log.TestSessionLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(HttpActionLogger.class);

    private final LogEntryService logEntryService;
    private final Project project;
    private final VideoRecorderService videoRecorderService;
    private final LogEntryStateImageMapper logEntryStateImageMapper;

    public HttpActionLogger(LogEntryService logEntryService, Project project,
                            VideoRecorderService videoRecorderService,
                            LogEntryStateImageMapper logEntryStateImageMapper) {
        this.logEntryService = logEntryService;
        this.project = project;
        this.videoRecorderService = videoRecorderService;
        this.logEntryStateImageMapper = logEntryStateImageMapper;
    }

    private Long getCurrentProjectId() {
        return project.getId();
    }

    @Override
    public LogData logAction(String sessionId, Matches matches, ObjectCollection objectCollection) {
        log.debug("Creating log entry - sessionId: {}, action: {}",
                sessionId, matches.getActionOptions().getAction());

        LogData logData = new LogData();
        logData.setProjectId(getCurrentProjectId());
        log.debug("Set projectId: {}", getCurrentProjectId());
        logData.setSessionId(sessionId);
        logData.setType(LogType.ACTION);
        logData.setActionType(matches.getActionOptions().getAction().toString());
        logData.setDescription(getStateImageDescription(objectCollection, matches)); // matches.getActionDescription());
        logData.setTimestamp(Instant.now());
        logData.setActionPerformed(matches.getActionOptions().getAction().toString());
        logData.setDuration(matches.getDuration().toMillis());
        logData.setSuccess(matches.isSuccess());
        logData.setCurrentStateName(getStateInFocus(objectCollection));
        objectCollection.getStateImages().forEach(
                sI -> {
                    logData.getStateImageLogData().add(logEntryStateImageMapper.toLog(sI, matches));
                    System.out.println(logData.getStateImageLogData());
                });
        LogData savedLog = logEntryService.saveLog(logData);
        log.debug("Saved log entry with id: {}", savedLog.getId());
        return savedLog;
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
    public LogData logStateTransition(String sessionId, Set<State> fromStates, Set<State> toStates,
                                      Set<State> beforeStates, boolean success, long transitionTime) {
        LogData logData = new LogData();
        logData.setSessionId(sessionId);
        logData.setType(LogType.TRANSITION);
        logData.setProjectId(getCurrentProjectId());
        String fromStatesString = fromStates.stream()
                .map(State::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("None");
        logData.setFromStates(fromStatesString);
        List<Long> fromStateIds = fromStates.stream()
                .map(State::getId)
                .toList();
        logData.setFromStateIds(fromStateIds);
        beforeStates.forEach(state -> {
            logData.getBeforeStateNames().add(state.getName());
            logData.getBeforeStateIds().add(state.getId());
        });
        toStates.forEach(state -> {
            logData.getToStateNames().add(state.getName());
            logData.getToStateIds().add(state.getId());
        });
        if (transitionTime > 0) {
            logData.setDuration(transitionTime);
            logData.setSuccess(success);
        }
        LogData savedLog = logEntryService.saveLog(logData);
        log.debug("Saved transition log with id: {} from {} to {}",
                savedLog.getId(), savedLog.getFromStates(), savedLog.getToStateNames());
        return savedLog;
    }

    @Override
    public LogData logObservation(String sessionId, String observationType, String description, String severity) {
        log.debug("Creating observation log entry - sessionId: {}, type: {}, description: {}, severity: {}",
                sessionId, observationType, description, severity);

        LogData logData = new LogData();
        logData.setProjectId(getCurrentProjectId());
        logData.setSessionId(sessionId);
        logData.setType(LogType.OBSERVATION);
        logData.setActionType(observationType);
        logData.setDescription(description);
        logData.setTimestamp(Instant.now());
        //logEntry.setSeverity(severity);

        LogData savedLog = logEntryService.saveLog(logData);
        log.debug("Saved observation log entry with id: {}", savedLog.getId());
        return savedLog;
    }

    @Override
    public LogData logPerformanceMetrics(String sessionId, long actionDuration, long pageLoadTime,
                                         long totalTestDuration) {
        LogData logData = new LogData();
        logData.setProjectId(getCurrentProjectId());
        logData.setSessionId(sessionId);
        logData.setType(LogType.METRICS);
        logData.setTimestamp(Instant.now());

        PerformanceMetricsData metrics = new PerformanceMetricsData();
        metrics.setActionDuration(actionDuration);
        metrics.setPageLoadTime(pageLoadTime);
        metrics.setTotalTestDuration(totalTestDuration);
        logData.setPerformance(metrics);

        return logEntryService.saveLog(logData);
    }

    @Override
    public LogData logError(String sessionId, String errorMessage, String screenshotPath) {
        LogData logData = new LogData();
        logData.setProjectId(getCurrentProjectId());
        logData.setSessionId(sessionId);
        logData.setType(LogType.ERROR);
        logData.setDescription(errorMessage);
        logData.setTimestamp(Instant.now());
        logData.setSuccess(false);
        logData.setScreenshotPath(screenshotPath);
        return logEntryService.saveLog(logData);
    }

    @Override
    public LogData startVideoRecording(String sessionId) throws IOException, AWTException {
        String fileName = "recordings/" + sessionId + "_" + System.currentTimeMillis() + ".avi";
        videoRecorderService.startRecording(fileName);

        LogData logData = new LogData();
        logData.setProjectId(getCurrentProjectId());
        logData.setSessionId(sessionId);
        logData.setType(LogType.VIDEO);
        logData.setDescription("Started video recording: " + fileName);
        logData.setTimestamp(Instant.now());
        return logEntryService.saveLog(logData);
    }

    @Override
    public LogData stopVideoRecording(String sessionId) throws IOException {
        videoRecorderService.stopRecording();

        LogData logData = new LogData();
        logData.setProjectId(getCurrentProjectId());
        logData.setSessionId(sessionId);
        logData.setType(LogType.VIDEO);
        logData.setDescription("Stopped video recording");
        logData.setTimestamp(Instant.now());
        return logEntryService.saveLog(logData);
    }

    @Override
    public String startSession(String applicationUnderTest) {
        String sessionId = UUID.randomUUID().toString();
        LogData logData = new LogData();
        logData.setType(LogType.SESSION);
        logData.setDescription("Started session for " + applicationUnderTest);
        logData.setSessionId(sessionId);
        logData.setProjectId(getCurrentProjectId());
        logData.setTimestamp(Instant.now());
        logEntryService.saveLog(logData);
        return sessionId;
    }

    @Override
    public void endSession(String sessionId) {
        LogData logData = new LogData(sessionId, LogType.SESSION, "Ended session");
        logEntryService.saveLog(logData);
    }

    @Override
    public void setCurrentState(String sessionId, String stateName, String stateDescription) {
        String description = String.format("Current State: %s - %s", stateName, stateDescription);
        LogData logData = new LogData(sessionId, LogType.SESSION, description);
        logEntryService.saveLog(logData);
    }
}
