package io.github.jspinak.brobot.log.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class LogEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long projectId = 0L;
    private String sessionId;

    @Enumerated(EnumType.STRING)
    private LogType type;
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
    private String fromStateName; // the transition starts from this state
    private Long fromStateId;
    private List<String> toStateNames = new ArrayList<>(); // the transition activates these states
    private List<Long> toStateIds = new ArrayList<>();
    private List<String> beforeStateNames = new ArrayList<>(); // the active states before the transition
    private List<Long> beforeStateIds = new ArrayList<>();
    private List<String> afterStateNames = new ArrayList<>(); // the actives states after the transition
        private List<Long> afterStateIds = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "log_entry_id", nullable = false)  // This creates the foreign key
    private List<StateImageLog> stateImages = new ArrayList<>();

    @Embedded
    private PerformanceMetrics performance;

    // Constructors
    public LogEntry() {
    }

    public LogEntry(String sessionId, LogType logType, String description) {
        this.sessionId = sessionId;
        this.type = logType;
        this.description = description;
    }
}