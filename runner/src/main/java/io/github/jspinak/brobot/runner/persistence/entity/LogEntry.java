package io.github.jspinak.brobot.runner.persistence.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.github.jspinak.brobot.tools.logging.model.LogEventType;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long projectId = 0L;
    private String sessionId;

    @Enumerated(EnumType.STRING)
    private LogEventType type;

    private String actionType;
    private String description;
    private Instant timestamp = Instant.now();
    private boolean success;
    private long duration;

    // Action-specific fields
    private String applicationUnderTest;
    private String actionPerformed;
    private String errorMessage;
    private String screenshotPath;
    private String videoClipPath;
    private String currentStateName;

    // Transition-specific fields
    private String fromStates; // the transition starts from this state
    private List<Long> fromStateIds;
    private List<String> toStateNames = new ArrayList<>(); // the transition activates these states
    private List<Long> toStateIds = new ArrayList<>();
    private List<String> beforeStateNames =
            new ArrayList<>(); // the active states before the transition
    private List<Long> beforeStateIds = new ArrayList<>();
    private List<String> afterStateNames =
            new ArrayList<>(); // the actives states after the transition
    private List<Long> afterStateIds = new ArrayList<>();

    @ElementCollection private List<StateImageLog> stateImageLogs = new ArrayList<>();

    @Embedded private PerformanceMetrics performance;

    // Constructors
    public LogEntry() {}

    public LogEntry(String sessionId, LogEventType logType, String description) {
        this.sessionId = sessionId;
        this.type = logType;
        this.description = description;
    }
}
