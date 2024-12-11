package io.github.jspinak.brobot.log.entities;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
public class LogEntryMapper {
    public LogEntry fromDTO(LogEntryDTO dto) {
        LogEntry log = new LogEntry();

        log.setProjectId(dto.getProjectId());
        log.setSessionId(dto.getSessionId());
        log.setType(LogType.valueOf(dto.getType()));
        log.setActionType(dto.getActionType());
        log.setDescription(dto.getDescription());
        log.setTimestamp(dto.getTimestamp());
        log.setSuccess(dto.isSuccess());
        log.setDuration(dto.getDuration());

        // Map action fields
        log.setApplicationUnderTest(dto.getApplicationUnderTest());
        log.setActionPerformed(dto.getActionPerformed());
        log.setErrorMessage(dto.getErrorMessage());
        log.setScreenshotPath(dto.getScreenshotPath());
        log.setVideoClipPath(dto.getVideoClipPath());
        log.setCurrentStateName(dto.getCurrentStateName());

        // Map transition fields
        log.setFromStateName(dto.getFromStateName());
        log.setFromStateId(dto.getFromStateId());
        log.setToStateNames(new ArrayList<>(dto.getToStateNames()));
        log.setToStateIds(new ArrayList<>(dto.getToStateIds()));
        log.setBeforeStateNames(new ArrayList<>(dto.getBeforeStateNames()));
        log.setBeforeStateIds(new ArrayList<>(dto.getBeforeStateIds()));
        log.setAfterStateNames(new ArrayList<>(dto.getAfterStateNames()));
        log.setAfterStateIds(new ArrayList<>(dto.getAfterStateIds()));

        // Map state image logs
        if (dto.getStateImageLogs() != null) {
            log.setStateImageLogs(
                    dto.getStateImageLogs().stream()
                            .map(this::mapStateImageLog)
                            .collect(Collectors.toList())
            );
        }

        // Map performance metrics
        if (dto.getPerformance() != null) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setActionDuration(dto.getPerformance().getActionDuration());
            metrics.setPageLoadTime(dto.getPerformance().getPageLoadTime());
            metrics.setTransitionTime(dto.getPerformance().getTransitionTime());
            metrics.setTotalTestDuration(dto.getPerformance().getTotalTestDuration());
            log.setPerformance(metrics);
        }

        return log;
    }

    private StateImageLog mapStateImageLog(StateImageLogDTO dto) {
        StateImageLog log = new StateImageLog();
        log.setStateImageId(dto.getStateImageId());
        log.setFound(dto.isFound());
        return log;
    }

    public LogEntryDTO toDTO(LogEntry log) {
        LogEntryDTO dto = new LogEntryDTO();

        dto.setId(log.getId());
        dto.setProjectId(log.getProjectId());
        dto.setSessionId(log.getSessionId());
        if (log.getType() == null) dto.setType(LogType.ACTION.toString());
        else dto.setType(log.getType().name());
        dto.setActionType(log.getActionType());
        dto.setDescription(log.getDescription());
        dto.setTimestamp(log.getTimestamp());
        dto.setSuccess(log.isSuccess());
        dto.setDuration(log.getDuration());

        // Map action fields
        dto.setApplicationUnderTest(log.getApplicationUnderTest());
        dto.setActionPerformed(log.getActionPerformed());
        dto.setErrorMessage(log.getErrorMessage());
        dto.setScreenshotPath(log.getScreenshotPath());
        dto.setVideoClipPath(log.getVideoClipPath());
        dto.setCurrentStateName(log.getCurrentStateName());

        // Map transition fields
        dto.setFromStateName(log.getFromStateName());
        dto.setFromStateId(log.getFromStateId());
        dto.setToStateNames(new ArrayList<>(log.getToStateNames()));
        dto.setToStateIds(new ArrayList<>(log.getToStateIds()));
        dto.setBeforeStateNames(new ArrayList<>(log.getBeforeStateNames()));
        dto.setBeforeStateIds(new ArrayList<>(log.getBeforeStateIds()));
        dto.setAfterStateNames(new ArrayList<>(log.getAfterStateNames()));
        dto.setAfterStateIds(new ArrayList<>(log.getAfterStateIds()));

        // Map state image logs
        if (log.getStateImageLogs() != null) {
            dto.setStateImageLogs(
                    log.getStateImageLogs().stream()
                            .map(this::mapStateImageLogToDTO)
                            .collect(Collectors.toList())
            );
        }

        // Map performance metrics
        if (log.getPerformance() != null) {
            PerformanceMetricsDTO metrics = new PerformanceMetricsDTO();
            metrics.setActionDuration(log.getPerformance().getActionDuration());
            metrics.setPageLoadTime(log.getPerformance().getPageLoadTime());
            metrics.setTransitionTime(log.getPerformance().getTransitionTime());
            metrics.setTotalTestDuration(log.getPerformance().getTotalTestDuration());
            dto.setPerformance(metrics);
        }

        return dto;
    }

    private StateImageLogDTO mapStateImageLogToDTO(StateImageLog log) {
        StateImageLogDTO dto = new StateImageLogDTO();
        dto.setStateImageId(log.getStateImageId());
        dto.setFound(log.isFound());
        return dto;
    }
}