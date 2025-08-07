package io.github.jspinak.brobot.runner.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a recording session that captures multiple ActionRecords.
 * A session groups related actions together for analysis and export.
 */
@Entity
@Table(name = "recording_sessions", indexes = {
    @Index(name = "idx_session_name", columnList = "session_name"),
    @Index(name = "idx_session_application", columnList = "application"),
    @Index(name = "idx_session_start_time", columnList = "start_time")
})
@Data
@EqualsAndHashCode(exclude = "actionRecords")
@ToString(exclude = "actionRecords")
public class RecordingSessionEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * User-defined name for the session
     */
    @Column(name = "session_name", nullable = false)
    private String name;
    
    /**
     * When the recording started
     */
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    /**
     * When the recording ended
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    /**
     * Total number of actions recorded
     */
    @Column(name = "total_actions")
    private int totalActions = 0;
    
    /**
     * Number of successful actions
     */
    @Column(name = "successful_actions")
    private int successfulActions = 0;
    
    /**
     * Number of failed actions
     */
    @Column(name = "failed_actions")
    private int failedActions = 0;
    
    /**
     * The application being automated
     */
    @Column(name = "application")
    private String application;
    
    /**
     * User-provided description of the session
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    /**
     * Whether this session has been exported
     */
    @Column(name = "exported")
    private boolean exported = false;
    
    /**
     * Path where the session was exported
     */
    @Column(name = "export_path", length = 500)
    private String exportPath;
    
    /**
     * Format of the last export (JSON, CSV, etc.)
     */
    @Column(name = "export_format", length = 20)
    private String exportFormat;
    
    /**
     * Whether this session was imported from external source
     */
    @Column(name = "imported")
    private boolean imported = false;
    
    /**
     * Tags for categorizing sessions (comma-separated)
     */
    @Column(name = "tags", length = 500)
    private String tags;
    
    /**
     * Session status (RECORDING, COMPLETED, FAILED, EXPORTED)
     */
    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    private SessionStatus status = SessionStatus.RECORDING;
    
    /**
     * All action records in this session
     */
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, 
               fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("timestamp ASC")
    private List<ActionRecordEntity> actionRecords = new ArrayList<>();
    
    /**
     * Session status enumeration
     */
    public enum SessionStatus {
        RECORDING,
        COMPLETED,
        FAILED,
        EXPORTED,
        ARCHIVED
    }
    
    /**
     * Helper method to add an action record
     */
    public void addActionRecord(ActionRecordEntity record) {
        actionRecords.add(record);
        record.setSession(this);
        totalActions++;
        if (record.isActionSuccess()) {
            successfulActions++;
        } else {
            failedActions++;
        }
    }
    
    /**
     * Helper method to remove an action record
     */
    public void removeActionRecord(ActionRecordEntity record) {
        actionRecords.remove(record);
        record.setSession(null);
        totalActions--;
        if (record.isActionSuccess()) {
            successfulActions--;
        } else {
            failedActions--;
        }
    }
    
    /**
     * Calculate the success rate
     */
    public double getSuccessRate() {
        if (totalActions == 0) return 0.0;
        return (double) successfulActions / totalActions * 100;
    }
    
    /**
     * Get the duration of the session
     */
    public Duration getDuration() {
        if (startTime == null) return Duration.ZERO;
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        return Duration.between(startTime, end);
    }
    
    /**
     * Check if the session is currently active
     */
    public boolean isActive() {
        return status == SessionStatus.RECORDING;
    }
    
    /**
     * Complete the session
     */
    public void complete() {
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        status = SessionStatus.COMPLETED;
    }
    
    /**
     * Mark session as failed
     */
    public void fail() {
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        status = SessionStatus.FAILED;
    }
    
    /**
     * Pre-persist callback
     */
    @PrePersist
    protected void onCreate() {
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
    }
}