package io.github.jspinak.brobot.runner.session.state;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * Represents a named snapshot of application state.
 *
 * <p>This class wraps an ApplicationState with additional metadata for identification and
 * management purposes.
 *
 * @since 1.0.0
 */
@Data
public class StateSnapshot {

    /** Unique identifier for this snapshot */
    private String id;

    /** Human-readable description of this snapshot */
    private String description;

    /** When this snapshot was created */
    private LocalDateTime timestamp;

    /** The actual application state */
    private ApplicationState applicationState;

    /** Optional tags for categorizing snapshots */
    private java.util.Set<String> tags;
}
