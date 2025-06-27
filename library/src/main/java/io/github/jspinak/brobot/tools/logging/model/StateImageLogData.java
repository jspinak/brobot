package io.github.jspinak.brobot.tools.logging.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents the result of a state image detection attempt during automated testing.
 * This lightweight model captures whether a specific state image was found during
 * pattern matching operations, providing essential data for debugging and analysis
 * without coupling the logging module to the core application domain.
 * 
 * <p>This class intentionally contains only the minimal data needed for logging purposes,
 * avoiding a direct reference to StateImageEntity to maintain proper module boundaries.</p>
 * 
 * <h3>Architectural Design Decision</h3>
 * <p>It's not practical to include the StateImageEntity because the Log module does not depend on the App module, and
 * we can't reference StateImageEntity in StateImageLog without creating a circular dependency.
 * While technically possible to move StateImageEntity to the Log module, it should remain in the App module for these reasons:</p>
 * <ul>
 *   <li><b>Domain Ownership:</b> StateImageEntity represents a core domain concept in the automation framework
 *       that's used for state management, UI interaction, and pattern matching. The Log module's primary
 *       responsibility is recording events and metrics, not defining core domain entities.</li>
 *   <li><b>Module Cohesion:</b> The App module has tightly coupled functionality around StateImages:
 *     <ul>
 *       <li>Pattern management (PatternEntity has a relationship with StateImageEntity)</li>
 *       <li>State management (StateEntity manages StateImages)</li>
 *       <li>Image processing and recognition</li>
 *       <li>Transaction definitions that use StateImages</li>
 *     </ul>
 *   </li>
 *   <li><b>Module Dependencies:</b> Moving StateImageEntity to the Log module would require moving related
 *       entities and services or splitting their functionality, which could lead to:
 *     <ul>
 *       <li>More complex dependency management</li>
 *       <li>Less maintainable codebase</li>
 *       <li>Potential leakage of App module concerns into the Log module</li>
 *     </ul>
 *   </li>
 *   <li><b>Module Boundaries:</b> The current design maintains clear module responsibilities:
 *     <ul>
 *       <li>Log module: Event recording, metrics, and monitoring</li>
 *       <li>App module: Core automation framework functionality</li>
 *     </ul>
 *   </li>
 * </ul>
 * 
 * @see LogData for the parent log entry that contains collections of these results
 * @see io.github.jspinak.brobot.tools.logging.dto.StateImageLogDTO for the corresponding DTO
 */
@Getter
@Setter
public class StateImageLogData {

    /**
     * The unique identifier of the state image that was searched for.
     * This ID references a StateImageEntity in the application module's database.
     */
    private Long stateImageId;
    
    /**
     * Indicates whether the state image was successfully detected on the screen.
     * true if the pattern matching algorithm found the image, false otherwise.
     */
    private boolean found;
}
