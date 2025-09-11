package io.github.jspinak.brobot.runner.persistence.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Entity for persisting ActionRecord data from brobot library execution. Captures the complete
 * execution context of an action including configuration, results, timing, and match information.
 */
@Entity
@Table(
        name = "action_records",
        indexes = {
            @Index(name = "idx_action_record_session", columnList = "session_id"),
            @Index(name = "idx_action_record_timestamp", columnList = "timestamp"),
            @Index(name = "idx_action_record_state", columnList = "state_name"),
            @Index(name = "idx_action_record_success", columnList = "action_success")
        })
@Data
@EqualsAndHashCode(exclude = {"session", "matches"})
@ToString(exclude = {"session", "matches"})
public class ActionRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The type of ActionConfig used (e.g., "PatternFindOptions", "ClickOptions") */
    @Column(name = "action_config_type", length = 100)
    private String actionConfigType;

    /** Serialized JSON representation of the ActionConfig */
    @Column(name = "action_config_json", columnDefinition = "TEXT")
    private String actionConfigJson;

    /** Whether the action was successful */
    @Column(name = "action_success", nullable = false)
    private boolean actionSuccess;

    /** Duration of the action execution in milliseconds */
    @Column(name = "duration_ms")
    private long duration;

    /** Text result from the action (e.g., OCR text) */
    @Column(name = "text_result", columnDefinition = "TEXT")
    private String text;

    /** When the action was executed */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /** The state ID from the brobot state machine */
    @Column(name = "state_id")
    private Long stateId;

    /** Name of the state where the action was executed */
    @Column(name = "state_name")
    private String stateName;

    /** Name of the StateObject (StateImage, StateString, etc.) */
    @Column(name = "object_name")
    private String objectName;

    /** The application under test */
    @Column(name = "application")
    private String applicationUnderTest;

    /** Additional metadata in JSON format */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    /** Screenshot path if captured */
    @Column(name = "screenshot_path", length = 500)
    private String screenshotPath;

    /** The recording session this action belongs to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private RecordingSessionEntity session;

    /** Match results from the action */
    @OneToMany(
            mappedBy = "actionRecord",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    private List<MatchEntity> matches = new ArrayList<>();

    /** Helper method to add a match */
    public void addMatch(MatchEntity match) {
        matches.add(match);
        match.setActionRecord(this);
    }

    /** Helper method to remove a match */
    public void removeMatch(MatchEntity match) {
        matches.remove(match);
        match.setActionRecord(null);
    }

    /** Pre-persist callback to set timestamp if not already set */
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
