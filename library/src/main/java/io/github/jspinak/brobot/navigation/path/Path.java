package io.github.jspinak.brobot.navigation.path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents a navigation path between states in the Brobot model-based GUI automation framework.
 *
 * <p>Path is a fundamental component of the Path Traversal Model (ξ), encapsulating a sequence of
 * states and the transitions between them that form a valid route through the GUI. Each path
 * represents a concrete way to navigate from one or more starting states to a target state,
 * enabling automated traversal of complex application workflows.
 *
 * <p>Key components:
 *
 * <ul>
 *   <li><b>State Sequence</b>: Ordered list of state IDs representing the navigation route
 *   <li><b>Transitions</b>: The state transition functions that move between consecutive states
 *   <li><b>Path Cost</b>: Numeric value for path quality (lower costs indicate preferred paths)
 * </ul>
 *
 * <p>Path costing:
 *
 * <ul>
 *   <li>Lower costs are preferred when multiple paths exist to the same target
 *   <li>Costs are calculated based on state weights and transition costs
 *   <li>Enables optimization of navigation efficiency and reliability
 * </ul>
 *
 * <p>Path operations:
 *
 * <ul>
 *   <li><b>Trimming</b>: Removes states before the first active state for partial execution
 *   <li><b>Cleaning</b>: Removes paths containing failed transitions
 *   <li><b>Reversal</b>: Changes direction for backward path construction
 *   <li><b>Copying</b>: Creates independent path instances for modification
 * </ul>
 *
 * <p>Use cases:
 *
 * <ul>
 *   <li>Storing discovered routes from PathFinder algorithms
 *   <li>Executing state transitions in sequence during automation
 *   <li>Recovering from failures by finding alternative paths
 *   <li>Optimizing navigation by selecting lowest-cost paths
 * </ul>
 *
 * <p>In the model-based approach, Path objects transform abstract state graphs into concrete
 * execution plans. They bridge the gap between the theoretical model of possible GUI states and the
 * practical requirements of navigating through them in a specific order to achieve automation
 * goals.
 *
 * @since 1.0
 * @see Paths
 * @see PathFinder
 * @see StateTransition
 * @see StateTransitions
 */
@Getter
@Setter
@Slf4j
public class Path {

    private List<Long> states = new ArrayList<>(); // all states in the path
    private List<StateTransition> transitions =
            new ArrayList<>(); // transitions between the states in the path
    private int pathCost; // lower costs are chosen first when selecting a path

    public boolean equals(Path path) {
        return this.states.equals(path.getStates());
    }

    public int size() {
        return states.size();
    }

    public boolean isEmpty() {
        return states.isEmpty();
    }

    public Long get(int i) {
        return states.get(i);
    }

    public void add(Long stateName) {
        states.add(stateName);
    }

    public void add(StateTransition transition) {
        transitions.add(transition);
    }

    public boolean contains(Long stateId) {
        return states.contains(stateId);
    }

    public void reverse() {
        Collections.reverse(states);
    }

    public boolean remove(Long stateId) {
        return states.remove(stateId);
    }

    public Path getCopy() {
        Path p = new Path();
        p.setStates(new ArrayList<>(states));
        p.setPathCost(pathCost);
        return p;
    }

    public void print() {
        log.debug("  Path (score {}): {}", pathCost, getStatesAsString());
    }

    public void print(StateService stateService) {
        log.debug("  Path (score {}): {}", pathCost, getPathAsString(stateService));
    }

    public String getPathAsString(StateService stateService) {
        StringBuilder sb = new StringBuilder();
        for (Long state : states) {
            String stateName =
                    stateService != null ? stateService.getStateName(state) : String.valueOf(state);
            sb.append(stateName).append(" -> ");
        }
        if (sb.length() > 0) sb.delete(sb.length() - 4, sb.length()); // remove last " -> "
        return sb.toString();
    }

    /**
     * If the Path contains a failed Transition, an empty Path will be returned. Otherwise, the Path
     * returned will begin at one of the activeStates.
     *
     * @param activeStates states that are currently visible.
     * @param failedTransitionStartState is a state corresponding to a path where the transition
     *     failed.
     * @return a Path object
     */
    public Path cleanPath(Set<Long> activeStates, Long failedTransitionStartState) {
        if (states.contains(failedTransitionStartState)) return new Path();
        return trimPath(activeStates);
    }

    public Path trimPath(Set<Long> activeStates) {
        Path trimmedPath = new Path();
        boolean addState = false;
        int i = 0;
        for (Long stateName : states) {
            // start adding states at an active state
            if (activeStates.contains(stateName)) addState = true;
            if (addState) {
                trimmedPath.add(stateName);
                // Only add transition if it exists (transitions list might be shorter than states)
                if (i < transitions.size()) {
                    trimmedPath.add(transitions.get(i));
                }
            }
            i++;
        }
        return trimmedPath;
    }

    public String getStatesAsString() {
        StringBuilder sb = new StringBuilder();
        for (Long state : states) {
            sb.append(state).append(" -> ");
        }
        if (sb.length() > 0) sb.delete(sb.length() - 4, sb.length()); // remove last " -> "
        return sb.toString();
    }
}
