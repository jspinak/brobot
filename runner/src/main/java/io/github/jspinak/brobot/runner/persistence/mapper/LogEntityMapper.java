package io.github.jspinak.brobot.runner.persistence.mapper;

import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.persistence.entity.LogEntry;
import io.github.jspinak.brobot.runner.persistence.entity.PerformanceMetrics;
import io.github.jspinak.brobot.runner.persistence.entity.StateImageLog;
import io.github.jspinak.brobot.tools.logging.model.ExecutionMetrics;
import io.github.jspinak.brobot.tools.logging.model.LogData;
import io.github.jspinak.brobot.tools.logging.model.StateImageLogData;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LogEntityMapper {

    /**
     * Maps the library's LogData object to the runner's persistable LogEntry entity.
     *
     * @param logData The data object from the library.
     * @return A persistable LogEntry entity.
     */
    public LogEntry toEntity(LogData logData) {
        if (logData == null) {
            return null;
        }

        LogEntry entity = new LogEntry();

        // Map simple fields
        entity.setProjectId(logData.getProjectId());
        entity.setSessionId(logData.getSessionId());
        entity.setType(logData.getType());
        entity.setActionType(logData.getActionType());
        entity.setDescription(logData.getDescription());
        entity.setTimestamp(logData.getTimestamp());
        entity.setSuccess(logData.isSuccess());
        entity.setDuration(logData.getDuration());
        entity.setApplicationUnderTest(logData.getApplicationUnderTest());
        entity.setActionPerformed(logData.getActionPerformed());
        entity.setErrorMessage(logData.getErrorMessage());
        entity.setScreenshotPath(logData.getScreenshotPath());
        entity.setVideoClipPath(logData.getVideoClipPath());
        entity.setCurrentStateName(logData.getCurrentStateName());
        entity.setFromStates(logData.getFromStates());
        entity.setToStateNames(logData.getToStateNames());
        entity.setBeforeStateNames(logData.getBeforeStateNames());
        entity.setAfterStateNames(logData.getAfterStateNames());

        // Map embedded PerformanceMetrics
        if (logData.getPerformance() != null) {
            entity.setPerformance(toPerformanceMetrics(logData.getPerformance()));
        }

        // Map list of StateImageLog
        if (logData.getStateImageLogData() != null) {
            entity.setStateImageLogs(
                    logData.getStateImageLogData().stream()
                            .map(this::toStateImageLog)
                            .collect(Collectors.toList()));
        } else {
            entity.setStateImageLogs(Collections.emptyList());
        }

        return entity;
    }

    private PerformanceMetrics toPerformanceMetrics(ExecutionMetrics data) {
        if (data == null) {
            return null;
        }
        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setActionDuration(data.getActionDuration());
        metrics.setPageLoadTime(data.getPageLoadTime());
        metrics.setTransitionTime(data.getTransitionTime());
        metrics.setTotalTestDuration(data.getTotalTestDuration());
        return metrics;
    }

    private StateImageLog toStateImageLog(StateImageLogData data) {
        if (data == null) {
            return null;
        }
        StateImageLog log = new StateImageLog();
        log.setStateImageId(data.getStateImageId());
        log.setFound(data.isFound());
        return log;
    }
}
