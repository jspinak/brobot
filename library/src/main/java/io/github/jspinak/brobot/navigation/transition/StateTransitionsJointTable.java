package io.github.jspinak.brobot.navigation.transition;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.special.SpecialStateType;
import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.navigation.service.StateService;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Maintains a comprehensive graph of state transitions for efficient navigation queries.
 * 
 * <p>StateTransitionsJointTable serves as a centralized index of all state relationships in 
 * the Brobot framework, maintaining both static transitions defined in the state structure 
 * and dynamic transitions to hidden states. This joint table enables efficient pathfinding 
 * and state relationship queries without traversing individual StateTransitions objects.</p>
 * 
 * <p>Table structure:
 * <ul>
 *   <li><b>Incoming Transitions</b>: Map of state ID to set of states that can navigate to it</li>
 *   <li><b>Outgoing Transitions</b>: Map of state ID to set of states it can navigate to</li>
 *   <li><b>Hidden State Transitions</b>: Dynamic map of hidden states to their hiding states</li>
 * </ul>
 * </p>
 * 
 * <p>Key features:
 * <ul>
 *   <li>Bidirectional navigation queries (who can reach X, where can Y go)</li>
 *   <li>Dynamic hidden state management based on active states</li>
 *   <li>Support for multi-target transitions</li>
 *   <li>Efficient O(1) lookup for state relationships</li>
 * </ul>
 * </p>
 * 
 * <p>Hidden state handling:
 * <ul>
 *   <li>When a state becomes active, its hidden states become accessible via PREVIOUS</li>
 *   <li>These dynamic transitions are added/removed as states activate/deactivate</li>
 *   <li>Enables navigation back to states that were covered by popups or overlays</li>
 *   <li>Tracked separately in incomingTransitionsToPREVIOUS map</li>
 * </ul>
 * </p>
 * 
 * <p>Common queries:
 * <ul>
 *   <li>"Which states can navigate to state X?" - getStatesWithTransitionsTo()</li>
 *   <li>"Where can state Y navigate to?" - getStatesWithTransitionsFrom()</li>
 *   <li>"What hidden states are currently accessible?" - check incomingTransitionsToPREVIOUS</li>
 * </ul>
 * </p>
 * 
 * <p>Integration points:
 * <ul>
 *   <li>Populated during initialization from StateTransitions objects</li>
 *   <li>Updated dynamically as states activate/deactivate</li>
 *   <li>Used by PathFinder for efficient path discovery</li>
 *   <li>Consulted during state navigation decisions</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, StateTransitionsJointTable provides the indexed view of 
 * the state graph that enables efficient navigation and pathfinding. By maintaining this 
 * pre-computed index, the framework can quickly answer reachability questions and find 
 * navigation paths without expensive graph traversals during automation execution.</p>
 * 
 * @since 1.0
 * @see StateTransitions
 * @see PathFinder
 * @see State
 * @see SpecialStateType
 */
@Component
@Getter
public class StateTransitionsJointTable {

    private final StateService stateService;
    
    Map<Long, Set<Long>> incomingTransitions = new HashMap<>();
    Map<Long, Set<Long>> outgoingTransitions = new HashMap<>();
    Map<Long, Set<Long>> incomingTransitionsToPREVIOUS = new HashMap<>(); // updated dynamically
    
    public StateTransitionsJointTable(StateService stateService) {
        this.stateService = stateService;
    }
    
    /**
     * Formats a state ID with its name for logging.
     * @param stateId The state ID to format
     * @return A string in the format "NAME(ID)" or just "ID" if name not found
     */
    private String formatStateNameAndId(Long stateId) {
        String name = stateService.getStateName(stateId);
        return name != null ? name + "(" + stateId + ")" : String.valueOf(stateId);
    }

    /**
     * Clears all transition tables for reinitialization.
     * <p>
     * Resets both static transition maps and dynamic hidden state transitions.
     * Used when rebuilding the state graph or during testing.
     */
    public void emptyRepos() {
        incomingTransitions = new HashMap<>();
        outgoingTransitions = new HashMap<>();
        incomingTransitionsToPREVIOUS = new HashMap<>();
    }

    /**
     * Adds dynamic transitions to states hidden by the newly active state.
     * <p>
     * When a state becomes active and hides other states, those hidden states
     * become accessible via the PREVIOUS mechanism. This method creates temporary
     * transitions from the active state to its hidden states, enabling back
     * navigation.
     * <p>
     * Side effects:
     * <ul>
     *   <li>Updates incomingTransitionsToPREVIOUS map</li>
     *   <li>Creates entries for new hidden states if needed</li>
     * </ul>
     *
     * @param activeState Newly activated state with hidden states
     * @see #removeTransitionsToHiddenStates(State)
     */
    public void addTransitionsToHiddenStates(State activeState) {
        activeState.getHiddenStateIds().forEach(hiddenState -> {
            if (!incomingTransitionsToPREVIOUS.containsKey(hiddenState)) {
                incomingTransitionsToPREVIOUS.put(hiddenState, new HashSet<>());
            }
            incomingTransitionsToPREVIOUS.get(hiddenState).add(activeState.getId());
        });
    }

    /**
     * Removes dynamic transitions to states that were hidden by the exited state.
     * <p>
     * When a state exits, its hidden states are no longer accessible via PREVIOUS
     * from that state. This cleanup maintains accurate navigation possibilities.
     * <p>
     * Note: May leave empty sets in the map which could be cleaned up periodically.
     *
     * @param exitedState State that is being deactivated
     * @see #addTransitionsToHiddenStates(State)
     */
    public void removeTransitionsToHiddenStates(State exitedState) {
        exitedState.getHiddenStateIds().forEach(hiddenState -> {
            if (incomingTransitionsToPREVIOUS.containsKey(hiddenState))
                incomingTransitionsToPREVIOUS.get(hiddenState).remove(exitedState.getId());
        });
    }

    /**
     * Populates the joint table with all transitions from a StateTransitions object.
     * <p>
     * Processes each transition to extract all state activation relationships,
     * creating entries for every (source state, target state) pair. This builds
     * the comprehensive navigation graph used for pathfinding.
     * <p>
     * Processing:
     * <ul>
     *   <li>Iterates through all transitions in the StateTransitions</li>
     *   <li>For each activated state, creates bidirectional entries</li>
     *   <li>Handles multi-target transitions correctly</li>
     * </ul>
     *
     * @param stateTransitions Container of transitions to process
     */
    public void addToJointTable(StateTransitions stateTransitions) {
        for (StateTransition transition : stateTransitions.getTransitions()) {
            for (Long child : transition.getActivate()) {
                add(child, stateTransitions.getStateId());
            }
        }
    }

    /**
     * Adds a single state-to-state transition relationship.
     * <p>
     * Creates both incoming and outgoing entries for efficient bidirectional
     * queries. PREVIOUS state is handled specially as it doesn't have incoming
     * transitions in the static table.
     *
     * @param to Target state ID
     * @param from Source state ID
     */
    public void add(Long to, Long from) {
        if (!from.equals(SpecialStateType.PREVIOUS.getId()))
            addIncomingTransition(to, from);
        addOutgoingTransition(to, from);
    }

    /**
     * Adds an incoming transition entry (parent -> child).
     *
     * @param child Target state receiving the transition
     * @param parentToAdd Source state initiating the transition
     */
    private void addIncomingTransition(Long child, Long parentToAdd) {
        if (incomingTransitions.containsKey(child)) incomingTransitions.get(child).add(parentToAdd);
        else {
            Set<Long> stateNames = new HashSet<>();
            stateNames.add(parentToAdd);
            incomingTransitions.put(child, stateNames);
        }
    }

    /**
     * Adds an outgoing transition entry (parent -> child).
     *
     * @param childToAdd Target state of the transition
     * @param parent Source state initiating the transition
     */
    private void addOutgoingTransition(Long childToAdd, Long parent) {
        if (outgoingTransitions.containsKey(parent)) outgoingTransitions.get(parent).add(childToAdd);
        else {
            Set<Long> stateNames = new HashSet<>();
            stateNames.add(childToAdd);
            outgoingTransitions.put(parent, stateNames);
        }
    }

    /**
     * Finds all states that can navigate to any of the specified target states.
     * <p>
     * Queries both static transitions and dynamic hidden state transitions.
     * Includes detailed logging for debugging pathfinding issues.
     *
     * @param children Variable number of target state IDs
     * @return Set of source state IDs that can reach the targets
     */
    public Set<Long> getStatesWithTransitionsTo(Long... children) {
        String childrenStr = Arrays.stream(children)
                .map(id -> formatStateNameAndId(id))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        System.out.println("Getting states with transitions to: [" + childrenStr + "]");
        
        Set<Long> parents = new HashSet<>();
        for (Long child : children) {
            String childStr = formatStateNameAndId(child);
            if (incomingTransitions.containsKey(child)) {
                parents.addAll(incomingTransitions.get(child));
                String parentsStr = incomingTransitions.get(child).stream()
                        .map(id -> formatStateNameAndId(id))
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");
                System.out.println("Found incoming transitions for " + childStr + ": [" + parentsStr + "]");
            } else {
                System.out.println("No incoming transitions found for " + childStr);
            }
            if (incomingTransitionsToPREVIOUS.containsKey(child)) {
                parents.addAll(incomingTransitionsToPREVIOUS.get(child));
                String prevParentsStr = incomingTransitionsToPREVIOUS.get(child).stream()
                        .map(id -> formatStateNameAndId(id))
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");
                System.out.println("Found incoming PREVIOUS transitions for " + childStr + ": [" + prevParentsStr + "]");
            } else {
                System.out.println("No incoming PREVIOUS transitions found for " + childStr);
            }
        }
        
        String allParentsStr = parents.stream()
                .map(id -> formatStateNameAndId(id))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        System.out.println("Returning parent states: [" + allParentsStr + "]");
        return parents;
    }

    /**
     * Finds all states that can navigate to any of the specified target states.
     * <p>
     * Set-based version for efficient bulk queries.
     *
     * @param children Set of target state IDs
     * @return Set of source state IDs that can reach the targets
     */
    public Set<Long> getStatesWithTransitionsTo(Set<Long> children) {
        Set<Long> parents = new HashSet<>();
        children.forEach(child -> {
            if (incomingTransitions.containsKey(child))
                parents.addAll(incomingTransitions.get(child));
            if (incomingTransitionsToPREVIOUS.containsKey(child))
                parents.addAll(incomingTransitionsToPREVIOUS.get(child));
        });
        return parents;
    }

    /**
     * Finds all states reachable from any of the specified source states.
     *
     * @param parents Variable number of source state IDs
     * @return Set of target state IDs reachable from the sources
     */
    public Set<Long> getStatesWithTransitionsFrom(Long... parents) {
        return getStatesWithTransitionsFrom(new HashSet<>(Arrays.asList(parents.clone())));
    }

    /**
     * Finds all states reachable from any of the specified source states.
     * <p>
     * Set-based version for efficient bulk queries.
     *
     * @param parents Set of source state IDs
     * @return Set of target state IDs reachable from the sources
     */
    public Set<Long> getStatesWithTransitionsFrom(Set<Long> parents) {
        Set<Long> children = new HashSet<>();
        parents.forEach(parent -> {
            if (outgoingTransitions.get(parent) != null) children.addAll(outgoingTransitions.get(parent));
        });
        return children;
    }

    /**
     * Merges static and dynamic incoming transitions into a single view.
     * <p>
     * Combines the permanent transitions defined in state structure with
     * temporary transitions to hidden states, providing a complete picture
     * of current navigation possibilities.
     *
     * @return Combined map of all incoming transitions
     */
    public Map<Long, Set<Long>> getIncomingTransitionsWithHiddenTransitions() {
        Map<Long, Set<Long>> allIncoming = new HashMap<>(incomingTransitions);
        incomingTransitionsToPREVIOUS.forEach((stateName, stateNames) -> {
            if (allIncoming.containsKey(stateName)) allIncoming.get(stateName).addAll(stateNames);
            else allIncoming.put(stateName, stateNames);
        });
        return allIncoming;
    }

    /**
     * Generates a formatted string representation of all transition tables.
     * <p>
     * Useful for debugging and visualizing the current state graph structure
     * including both static and dynamic transitions.
     *
     * @return Formatted string showing all transition relationships
     */
    public String print() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("StateTransitionsJointTable\n");
        for (Long incomingStateId : incomingTransitions.keySet()) {
            stringBuilder.append("incoming transitions to state ").append(incomingStateId.toString()).append(": ");
            incomingTransitions.get(incomingStateId).forEach(fromStateId -> stringBuilder.append(fromStateId).append(" "));
        }
        stringBuilder.append("\n");
        for (Long outgoingStateId : outgoingTransitions.keySet()) {
            stringBuilder.append("outgoing transitions to state ").append(outgoingStateId.toString()).append(": ");
            outgoingTransitions.get(outgoingStateId).forEach(fromStateId -> stringBuilder.append(fromStateId).append(" "));
        }
        stringBuilder.append("\n");
        for (Long previousStateId : incomingTransitionsToPREVIOUS.keySet()) {
            stringBuilder.append("incoming transitions to PREVIOUS ").append(previousStateId.toString()).append(": ");
            incomingTransitionsToPREVIOUS.get(previousStateId).forEach(fromStateId -> stringBuilder.append(fromStateId).append(" "));
        }
        System.out.println(stringBuilder);
        return stringBuilder.toString();
    }

}
