package io.github.jspinak.brobot.log.entities;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a log entry for a state image.
 * It's not practical to include the StateImageEntity because the Log module does not depend on the App module, and
 * we can't reference StateImageEntity in StateImageLog without creating a circular dependency.
 * While technically possible to move StateImageEntity to the Log module, it should remain in the App module for these reasons:
 *    Domain Ownership: StateImageEntity represents a core domain concept in the automation framework that's used for state management, UI interaction, and pattern matching. The Log module's primary responsibility is recording events and metrics, not defining core domain entities.
 *    Module Cohesion: The App module has tightly coupled functionality around StateImages:
 *      Pattern management (PatternEntity has a relationship with StateImageEntity)
 *      State management (StateEntity manages StateImages)
 *      Image processing and recognition
 *      Transaction definitions that use StateImages
 *    Module Dependencies: Moving StateImageEntity to the Log module would require moving related entities and services or splitting their functionality, which could lead to:
 *      More complex dependency management
 *      Less maintainable codebase
 *      Potential leakage of App module concerns into the Log module
 *    Module Boundaries: The current design maintains clear module responsibilities:
 *      Log module: Event recording, metrics, and monitoring
 *      App module: Core automation framework functionality
 */
@Embeddable
@Getter
@Setter
public class StateImageLog {

    private Long stateImageId;
    private boolean found;
}
