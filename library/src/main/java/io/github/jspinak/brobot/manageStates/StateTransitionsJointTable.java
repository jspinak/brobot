package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.datatypes.state.state.State;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Holds all static incoming and outgoing Transitions,
 * plus the variable transitions to hidden States.
 * Variable transitions to hidden States, found in the
 * 'incomingTransitionsToPREVIOUS' set, are updated
 * when hidden States are added to or removed from a State.
 */
@Component
@Getter
public class StateTransitionsJointTable {

    Map<Long, Set<Long>> incomingTransitions = new HashMap<>();
    Map<Long, Set<Long>> outgoingTransitions = new HashMap<>();
    Map<Long, Set<Long>> incomingTransitionsToPREVIOUS = new HashMap<>(); // updated dynamically

    /**
     * Hidden States can be accessed by closing the State hiding them. There may be multiple States
     * hiding different hidden States. When a new State is activated that hides States, these
     * transitions are added here.
     * @param activeState is a newly active State.
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
     * When a State is exited, its hidden State transitions are removed.
     * This may leave some Map entries with empty Lists.
     * @param exitedState is a State that is no longer active.
     */
    public void removeTransitionsToHiddenStates(State exitedState) {
        exitedState.getHiddenStateNames().forEach(hiddenState -> {
            if (incomingTransitionsToPREVIOUS.containsKey(hiddenState))
                incomingTransitionsToPREVIOUS.get(hiddenState).remove(exitedState.getName());
        });
    }

    public void add(Long to, Long from) {
        if (!from.equals(StateMemory.Enum.PREVIOUS.toString())) addIncomingTransition(to, from);
        addOutgoingTransition(to, from);
    }

    private void addIncomingTransition(Long child, Long parentToAdd) {
        if (incomingTransitions.containsKey(child)) incomingTransitions.get(child).add(parentToAdd);
        else {
            Set<Long> stateNames = new HashSet<>();
            stateNames.add(parentToAdd);
            incomingTransitions.put(child, stateNames);
        }
    }

    private void addOutgoingTransition(Long childToAdd, Long parent) {
        if (outgoingTransitions.containsKey(parent)) outgoingTransitions.get(parent).add(childToAdd);
        else {
            Set<Long> stateNames = new HashSet<>();
            stateNames.add(childToAdd);
            outgoingTransitions.put(parent, stateNames);
        }
    }

    public Set<Long> getStatesWithTransitionsTo(Long... children) {
        return getStatesWithTransitionsTo(new HashSet<>(Arrays.asList(children.clone())));
    }

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

    public Set<Long> getStatesWithTransitionsFrom(Long... parents) {
        return getStatesWithTransitionsFrom(new HashSet<>(Arrays.asList(parents.clone())));
    }

    public Set<Long> getStatesWithTransitionsFrom(Set<Long> parents) {
        Set<Long> children = new HashSet<>();
        parents.forEach(parent -> {
            if (outgoingTransitions.get(parent) != null) children.addAll(outgoingTransitions.get(parent));
        });
        return children;
    }

    public Map<Long, Set<Long>> getIncomingTransitionsWithHiddenTransitions() {
        Map<Long, Set<Long>> allIncoming = new HashMap<>();
        allIncoming.putAll(incomingTransitions);
        incomingTransitionsToPREVIOUS.forEach((stateName, stateNames) -> {
            if (allIncoming.containsKey(stateName)) allIncoming.get(stateName).addAll(stateNames);
            else allIncoming.put(stateName, stateNames);
        });
        return allIncoming;
    }

    public Map<Long, Set<Long>> getOutgoingTransitions() {
        return outgoingTransitions;
    }
}
