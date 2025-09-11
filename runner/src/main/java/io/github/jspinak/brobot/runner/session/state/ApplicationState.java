package io.github.jspinak.brobot.runner.session.state;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import io.github.jspinak.brobot.navigation.transition.StateTransitions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a captured snapshot of the application state.
 *
 * <p>This class contains all the information needed to restore the application to a previous state.
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationState {

    /** Time when this state was captured */
    private LocalDateTime captureTime;

    /** List of state transitions at the time of capture */
    private List<StateTransitions> stateTransitions;

    /** Set of active state IDs at the time of capture */
    private Set<Long> activeStateIds;

    /** Additional metadata about the state */
    private java.util.Map<String, Object> metadata;

    /** Last modification time of this state */
    private LocalDateTime lastModified;
}
