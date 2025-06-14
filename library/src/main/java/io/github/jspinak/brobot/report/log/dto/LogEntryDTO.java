package io.github.jspinak.brobot.report.log.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class LogEntryDTO {
    private Long id;
    private Long projectId;
    private String sessionId;
    private String type;  // String representation of LogType enum
    private String actionType;
    private String description;
    private Instant timestamp;
    private boolean success;
    private long duration;
    private String applicationUnderTest;
    private String actionPerformed;
    private String errorMessage;
    private String screenshotPath;
    private String videoClipPath;
    private String currentStateName;
    private String fromStateName;
    private List<Long> fromStateIds;
    private List<String> toStateNames;
    private List<Long> toStateIds;
    private List<String> beforeStateNames;
    private List<Long> beforeStateIds;
    private List<String> afterStateNames;
    private List<Long> afterStateIds;
    private List<StateImageLogDTO> stateImageLogs;
    private PerformanceMetricsDTO performance;
}
