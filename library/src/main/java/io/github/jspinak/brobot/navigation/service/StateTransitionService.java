package io.github.jspinak.brobot.navigation.service;

import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.model.state.special.SpecialStateType;
import io.github.jspinak.brobot.model.transition.StateTransitionStore;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.navigation.transition.StateTransitionsJointTable;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service layer for managing state transitions within the active project.
 * 
 * <p>StateTransitionService provides comprehensive access to state transition information, 
 * handling the complexity of transition resolution including special cases like hidden states 
 * and PREVIOUS navigation. It serves as the bridge between high-level navigation requests 
 * and the underlying transition repository, ensuring correct transition selection even in 
 * complex GUI scenarios with overlapping states.</p>
 * 
 * <p>Key responsibilities:
 * <ul>
 *   <li><b>Transition Resolution</b>: Find the correct transition between any two states</li>
 *   <li><b>Hidden State Handling</b>: Resolve PREVIOUS transitions for hidden states</li>
 *   <li><b>Repository Management</b>: Provide access to transition collections</li>
 *   <li><b>Joint Table Maintenance</b>: Keep the transition index synchronized</li>
 *   <li><b>Statistics Management</b>: Track and reset transition success metrics</li>
 * </ul>
 * </p>
 * 
 * <p>Transition resolution logic:
 * <ol>
 *   <li>Check for direct transition from source to target state</li>
 *   <li>If not found, check if target is a hidden state of source</li>
 *   <li>Return PREVIOUS if hidden state transition is valid</li>
 *   <li>Return NULL if no valid transition exists</li>
 * </ol>
 * </p>
 * 
 * <p>Special state handling:
 * <ul>
 *   <li><b>PREVIOUS</b>: Represents navigation to states hidden by the current state</li>
 *   <li><b>NULL</b>: Indicates no valid transition path exists</li>
 *   <li><b>Hidden States</b>: States covered by overlays but still conceptually present</li>
 * </ul>
 * </p>
 * 
 * <p>Common usage patterns:
 * <ul>
 *   <li>Resolve transitions during path planning</li>
 *   <li>Access transition definitions for execution</li>
 *   <li>Query available transitions from a state</li>
 *   <li>Reset metrics for new automation runs</li>
 * </ul>
 * </p>
 * 
 * <p>Performance considerations:
 * <ul>
 *   <li>Uses StateTransitionsJointTable for O(1) transition lookups</li>
 *   <li>Caches transition relationships in joint table</li>
 *   <li>Returns Optional to handle missing transitions gracefully</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, StateTransitionService encapsulates the intelligence 
 * needed to navigate complex state relationships. It understands not just direct transitions 
 * but also the nuanced relationships created by GUI layering, enabling robust automation 
 * that can handle popups, dialogs, and other overlapping UI elements.</p>
 * 
 * @since 1.0
 * @see StateTransitions
 * @see StateTransition
 * @see StateTransitionsJointTable
 * @see StateTransitionStore
 * @see SpecialStateType
 */
@Component
@Getter
public class StateTransitionService {

    private final StateTransitionStore stateTransitionsRepository;
    private final StateTransitionsJointTable stateTransitionsJointTable;

    private final Set<Long> statesToActivate = new HashSet<>();

    public StateTransitionService(StateTransitionStore stateTransitionsRepository,
                                            StateTransitionsJointTable stateTransitionsJointTable) {
        this.stateTransitionsRepository = stateTransitionsRepository;
        this.stateTransitionsJointTable = stateTransitionsJointTable;
    }

    public List<StateTransitions> getAllStateTransitionsInstances() {
        return stateTransitionsRepository.getAllStateTransitionsAsCopy();
    }

    public List<StateTransitions> getAllStateTransitions() {
        return stateTransitionsRepository.getRepo();
    }

    /**
     * Initializes the transition repository and joint table.
     * <p>
     * Populates the StateTransitionsJointTable with all transition
     * relationships for efficient lookup during navigation.
     * Should be called after all transitions are loaded.
     */
    public void setupRepo() {
        stateTransitionsRepository.populateStateTransitionsJointTable();
    }

    public List<StateTransition> getAllIndividualTransitions() {
        return stateTransitionsRepository.getAllTransitions();
    }

    /**
     * Resolves the correct transition type between two states.
     * <p>
     * Determines whether a transition is direct, through PREVIOUS (hidden state),
     * or invalid (NULL). This resolution is crucial for handling complex GUI
     * scenarios where states may be hidden by overlays.
     * <p>
     * Resolution logic:
     * <ol>
     *   <li>Direct transition: Returns target state ID</li>
     *   <li>Hidden state transition: Returns PREVIOUS ID</li>
     *   <li>No valid transition: Returns NULL ID</li>
     * </ol>
     *
     * @param from Source state ID
     * @param to Target state ID
     * @return Resolved transition type (target ID, PREVIOUS, or NULL)
     */
    public Long getTransitionToEnum(Long from, Long to) {
        // check first if it is a normal transition
        if (stateTransitionsJointTable.getStatesWithTransitionsFrom(from).contains(to)) return to;
        // if not, it may be a hidden state transition
        if (!stateTransitionsJointTable.getIncomingTransitionsToPREVIOUS().containsKey(to) ||
            !stateTransitionsJointTable.getIncomingTransitionsToPREVIOUS().get(to).contains(from))
            return SpecialStateType.NULL.getId(); // it is not a hidden state transition either
        return SpecialStateType.PREVIOUS.getId(); // it is a hidden state transition
    }

    /**
     * Retrieves all transitions defined for a specific state.
     *
     * @param stateId ID of the state
     * @return Optional containing StateTransitions if found
     */
    public Optional<StateTransitions> getTransitions(Long stateId) {
        return stateTransitionsRepository.get(stateId);
    }

    /**
     * Retrieves a specific transition between two states.
     * <p>
     * Finds the exact transition definition that navigates from
     * the source state to the target state.
     *
     * @param fromState Source state ID
     * @param toState Target state ID
     * @return Optional containing the transition if found
     */
    public Optional<StateTransition> getTransition(Long fromState, Long toState) {
        Optional<StateTransitions> transitions = getTransitions(fromState);
        if (transitions.isEmpty()) return Optional.empty();
        return transitions.get().getTransitionFunctionByActivatedStateId(toState);
    }

    /**
     * Resets success counters for all transitions.
     * <p>
     * Clears transition success statistics, useful for starting
     * fresh automation runs or resetting performance metrics.
     * Side effect: modifies all transitions in repository.
     */
    public void resetTimesSuccessful() {
        stateTransitionsRepository.getAllTransitions().forEach(transition ->
                transition.setTimesSuccessful(0));
    }

    /**
     * Prints all transitions to the report for debugging.
     * <p>
     * Useful for verifying transition definitions and
     * troubleshooting navigation issues.
     */
    public void printAllTransitions() {
        ConsoleReporter.print("State Transitions in Project:\n");
        stateTransitionsRepository.getAllTransitions().forEach(transition ->
                ConsoleReporter.println(transition.toString()));
        ConsoleReporter.println();
    }

}
