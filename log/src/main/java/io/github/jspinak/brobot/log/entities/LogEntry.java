package io.github.jspinak.brobot.log.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "log_entries", indexes = {
        @Index(name = "idx_session_id", columnList = "sessionId"),
        @Index(name = "idx_passed", columnList = "passed"),
        @Index(name = "idx_current_state_name", columnList = "currentStateName"),
        @Index(name = "idx_type", columnList = "type"),
        @Index(name = "idx_project_id", columnList = "projectId")
})
@Getter
@Setter
public class LogEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    private String sessionId;
    private String type;
    private String description;
    private Instant timestamp;
    private String applicationUnderTest;
    private String currentStateName;  // TODO: change to stateInFocus, representing the state acted on
    private boolean passed;
    private String actionPerformed;
    private long duration; // in milliseconds
    private String errorMessage;
    private String screenshotPath;
    private String videoClipPath;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Observation> observations = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "log_entry_id")
    private List<StateImageLog> stateImages = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransitionLog> transitions = new ArrayList<>();

    @Embedded
    private PerformanceMetrics performance;

    public LogEntry() {
        this.projectId = 0L; // Default project ID
    }

    public LogEntry(String sessionId, String type, String description) {
        this.sessionId = sessionId;
        this.type = type;
        this.description = description;
        this.projectId = 0L;
    }

    public void addStateImageLog(StateImageLog stateImageLog) {
        this.stateImages.add(stateImageLog);
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", sessionId='" + sessionId + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", timestamp=" + timestamp +
                ", applicationUnderTest='" + applicationUnderTest + '\'' +
                ", currentStateName='" + currentStateName + '\'' +
                ", passed=" + passed +
                ", actionPerformed='" + actionPerformed + '\'' +
                ", duration=" + duration +
                ", errorMessage='" + errorMessage + '\'' +
                ", screenshotPath='" + screenshotPath + '\'' +
                ", videoClipPath='" + videoClipPath + '\'' +
                ", observations=" + observations +
                ", stateImages=" + stateImages +
                ", transitions=" + transitions +
                ", performance=" + performance +
                '}';
    }
}
