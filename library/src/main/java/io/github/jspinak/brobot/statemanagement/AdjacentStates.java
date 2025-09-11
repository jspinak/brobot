package io.github.jspinak.brobot.statemanagement;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.special.SpecialStateType;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;

/**
 * Discovers states reachable through direct transitions from given states.
 *
 * <p>AdjacentStates is a critical component in the Brobot framework's State Structure (S),
 * responsible for identifying which states can be reached through single transitions from the
 * current position in the GUI. This forms the basis for local navigation decisions and helps build
 * the state graph dynamically during automation execution.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li><b>Transition Analysis</b>: Examines StateTransitions to find reachable states
 *   <li><b>Static Transition Support</b>: Identifies states with defined activation methods
 *   <li><b>Hidden State Handling</b>: Manages PREVIOUS state expansion to hidden states
 *   <li><b>Multi-State Processing</b>: Can process single states or sets of states
 * </ul>
 *
 * <p>Adjacent state discovery process:
 *
 * <ol>
 *   <li>Retrieve all transitions defined for the source state
 *   <li>Filter transitions that have activation methods (non-empty activate list)
 *   <li>Extract target state IDs from these transitions
 *   <li>Handle special PREVIOUS state by expanding to hidden states
 *   <li>Return the complete set of reachable states
 * </ol>
 *
 * <p>Special handling for PREVIOUS state:
 *
 * <ul>
 *   <li>PREVIOUS is a special state representing "go back" functionality
 *   <li>When PREVIOUS is adjacent, it's replaced with actual hidden states
 *   <li>Hidden states represent the concrete states that "back" would navigate to
 *   <li>This enables proper pathfinding through back navigation
 * </ul>
 *
 * <p>Use cases:
 *
 * <ul>
 *   <li>Building local state graphs for pathfinding algorithms
 *   <li>Determining available navigation options from current position
 *   <li>Validating transition definitions during state model construction
 *   <li>Supporting breadth-first search in state space exploration
 * </ul>
 *
 * <p>In the model-based approach, AdjacentStates provides the foundation for understanding the
 * local connectivity of the state graph. Unlike process-based automation that follows fixed
 * scripts, this component enables dynamic discovery of navigation possibilities, allowing the
 * framework to adapt to GUI variations and find alternative paths when needed.
 *
 * <p>The ability to discover adjacent states dynamically is crucial for:
 *
 * <ul>
 *   <li>Recovering from unexpected GUI states
 *   <li>Finding optimal navigation paths
 *   <li>Adapting to application updates that change navigation flow
 *   <li>Building resilient automation that handles GUI variations
 * </ul>
 *
 * @since 1.0
 * @see StateTransitions
 * @see StateMemory
 * @see State
 * @see SpecialStateType
 */
@Component
public class AdjacentStates {

    private final StateService allStatesInProjectService;
    private final StateMemory stateMemory;
    private final StateTransitionService stateTransitionsInProjectService;

    public AdjacentStates(
            StateService allStatesInProjectService,
            StateMemory stateMemory,
            StateTransitionService stateTransitionsInProjectService) {
        this.allStatesInProjectService = allStatesInProjectService;
        this.stateMemory = stateMemory;
        this.stateTransitionsInProjectService = stateTransitionsInProjectService;
    }

    /**
     * Discovers all states directly reachable from a single source state.
     *
     * <p>Analyzes the StateTransitions defined for the given state to identify which states can be
     * reached through a single transition. Only considers transitions that have activation methods
     * defined (non-empty activate list), ensuring that theoretical transitions without
     * implementation are excluded.
     *
     * <p>Special handling for PREVIOUS state: When PREVIOUS appears in the adjacent states, it is
     * expanded to the actual hidden states it represents, providing concrete navigation targets for
     * back functionality.
     *
     * @param stateId The ID of the state to find adjacent states for
     * @return Set of state IDs that can be reached through direct transitions, empty set if the
     *     state has no transitions or doesn't exist
     * @see StateTransitions#getTransitions()
     * @see SpecialStateType#PREVIOUS
     */
    public Set<Long> getAdjacentStates(Long stateId) {
        Set<Long> adjacent = new HashSet<>();
        Optional<StateTransitions> trsOpt =
                stateTransitionsInProjectService.getTransitions(stateId);
        if (trsOpt.isEmpty()) return adjacent;
        Set<Long> statesWithStaticTransitions =
                trsOpt.get().getTransitions().stream()
                        .filter(t -> t.getActivate() != null && !t.getActivate().isEmpty())
                        .flatMap(t -> t.getActivate().stream())
                        .collect(Collectors.toSet());
        adjacent.addAll(statesWithStaticTransitions);
        if (!statesWithStaticTransitions.contains(SpecialStateType.PREVIOUS.getId()))
            return adjacent;
        adjacent.remove(SpecialStateType.PREVIOUS.getId());
        Optional<State> currentState = allStatesInProjectService.getState(stateId);
        if (currentState.isEmpty() || currentState.get().getHiddenStateIds().isEmpty())
            return adjacent;
        adjacent.addAll(currentState.get().getHiddenStateIds());
        return adjacent;
    }

    /**
     * Discovers all states directly reachable from multiple source states.
     *
     * <p>Aggregates the adjacent states from all provided source states into a single set. This is
     * useful when multiple states are active simultaneously or when exploring navigation options
     * from a set of possible current positions.
     *
     * @param stateIds Set of state IDs to find adjacent states for
     * @return Combined set of all states reachable from any of the source states
     */
    public Set<Long> getAdjacentStates(Set<Long> stateIds) {
        Set<Long> adjacent = new HashSet<>();
        stateIds.forEach(sE -> adjacent.addAll(getAdjacentStates(sE)));
        return adjacent;
    }

    /**
     * Discovers all states directly reachable from the currently active states.
     *
     * <p>Convenience method that uses StateMemory to determine the current active states and finds
     * all adjacent states from that position. This represents the current navigation options
     * available to the automation framework.
     *
     * @return Set of state IDs reachable from the current GUI position
     * @see StateMemory#getActiveStates()
     */
    public Set<Long> getAdjacentStates() {
        return getAdjacentStates(stateMemory.getActiveStates());
    }
}
