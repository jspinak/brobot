package io.github.jspinak.brobot.statemanagement;

import java.util.List;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.transition.JavaStateTransition;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;

/**
 * Service for resolving state names to IDs in state transitions.
 *
 * <p>StateIdResolver handles the critical task of converting state names (used in code for
 * readability) to state IDs (used internally for efficient processing). This service specifically
 * processes StateTransitions objects to replace human-readable state names with their corresponding
 * numeric identifiers.
 *
 * <p>Key responsibilities:
 *
 * <ul>
 *   <li>Convert state names to IDs within StateTransitions objects
 *   <li>Process JavaStateTransition activate lists to resolve state names
 *   <li>Perform batch conversion during application initialization
 * </ul>
 *
 * <p>This service is essential for the model-based approach as it bridges the gap between
 * human-readable state definitions and machine-efficient state processing, ensuring both developer
 * productivity and runtime performance.
 *
 * @since 1.0
 * @see StateTransitions
 * @see JavaStateTransition
 * @see StateService
 */
@Service
public class StateIdResolver {

    private final StateService allStatesInProjectService;

    public StateIdResolver(StateService allStatesInProjectService) {
        this.allStatesInProjectService = allStatesInProjectService;
    }

    /**
     * Batch converts state names to IDs for all state transitions.
     *
     * <p>Processes a collection of StateTransitions objects, converting human-readable state names
     * to numeric IDs for efficient runtime processing. This bulk operation is typically performed
     * during application initialization after all states have been registered.
     *
     * <p>Side effects:
     *
     * <ul>
     *   <li>Modifies StateTransitions objects in-place
     *   <li>Sets state IDs based on registered state names
     *   <li>Updates JavaStateTransition activate lists with IDs
     * </ul>
     *
     * @param allStateTransitions List of StateTransitions to process
     * @see #convertNamesToIds(StateTransitions)
     */
    public void convertAllStateTransitions(List<StateTransitions> allStateTransitions) {
        for (StateTransitions stateTransitions : allStateTransitions) {
            convertNamesToIds(stateTransitions);
        }
    }

    /**
     * Converts state names to IDs within a single StateTransitions object.
     *
     * <p>Performs name-to-ID conversion for both the parent state and all target states referenced
     * in its transitions. This enables the framework to use efficient numeric comparisons during
     * runtime while allowing developers to define states using meaningful names.
     *
     * <p>Conversion process:
     *
     * <ol>
     *   <li>Convert the parent state name to ID if not already set
     *   <li>For JavaStateTransition objects, convert activate state names
     *   <li>Build parallel lists of state IDs for runtime use
     * </ol>
     *
     * <p>Prerequisites:
     *
     * <ul>
     *   <li>State names must be unique within the project
     *   <li>All referenced states must be registered in StateService
     *   <li>StateService must be initialized with all states
     * </ul>
     *
     * <p>Side effects:
     *
     * <ul>
     *   <li>Sets stateId field if currently null
     *   <li>Populates activate ID lists in JavaStateTransition objects
     *   <li>Silently ignores unregistered state names
     * </ul>
     *
     * @param stateTransitions The StateTransitions object to process
     * @see StateService#getStateId(String)
     * @see JavaStateTransition
     */
    public void convertNamesToIds(StateTransitions stateTransitions) {
        Long stateId = allStatesInProjectService.getStateId(stateTransitions.getStateName());
        if (stateTransitions.getStateId() == null) {
            stateTransitions.setStateId(stateId);
        }
        for (StateTransition transition : stateTransitions.getTransitions()) {
            if (transition instanceof JavaStateTransition javaTransition) {
                for (String stateToActivate : javaTransition.getActivateNames()) {
                    Long stateToActivateId = allStatesInProjectService.getStateId(stateToActivate);
                    if (stateToActivateId != null) {
                        javaTransition.getActivate().add(stateToActivateId);
                    }
                }
            }
        }
    }
}
