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

    Map<String, Set<String>> incomingTransitions = new HashMap<>();
    Map<String, Set<String>> outgoingTransitions = new HashMap<>();
    Map<String, Set<String>> incomingTransitionsToPREVIOUS = new HashMap<>(); // updated dynamically

    /**
     * Hidden States can be accessed by closing the State hiding them. There may be multiple States
     * hiding different hidden States. When a new State is activated that hides States, these
     * transitions are added here.
     * @param activeState is a newly active State.
     */
    public void addTransitionsToHiddenStates(State activeState) {
        activeState.getHidden().forEach(hiddenState -> {
            if (!incomingTransitionsToPREVIOUS.containsKey(hiddenState)) {
                incomingTransitionsToPREVIOUS.put(hiddenState, new HashSet<>());
            }
            incomingTransitionsToPREVIOUS.get(hiddenState).add(activeState.getName());
        });
    }

    /**
     * When a State is exited, its hidden State transitions are removed.
     * This may leave some Map entries with empty Lists.
     * @param exitedState is a State that is no longer active.
     */
    public void removeTransitionsToHiddenStates(State exitedState) {
        exitedState.getHidden().forEach(hiddenState -> {
            if (incomingTransitionsToPREVIOUS.containsKey(hiddenState))
                incomingTransitionsToPREVIOUS.get(hiddenState).remove(exitedState.getName());
        });
    }

    public void add(String to, String from) {
        if (!from.equals(StateMemory.Enum.PREVIOUS.toString())) addIncomingTransition(to, from);
        addOutgoingTransition(to, from);
    }

    private void addIncomingTransition(String child, String parentToAdd) {
        if (incomingTransitions.containsKey(child)) incomingTransitions.get(child).add(parentToAdd);
        else {
            Set<String> stateNames = new HashSet<>();
            stateNames.add(parentToAdd);
            incomingTransitions.put(child, stateNames);
        }
    }

    private void addOutgoingTransition(String childToAdd, String parent) {
        if (outgoingTransitions.containsKey(parent)) outgoingTransitions.get(parent).add(childToAdd);
        else {
            Set<String> stateNames = new HashSet<>();
            stateNames.add(childToAdd);
            outgoingTransitions.put(parent, stateNames);
        }
    }

    public Set<String> getStatesWithTransitionsTo(String... children) {
        return getStatesWithTransitionsTo(new HashSet<>(Arrays.asList(children.clone())));
    }

    public Set<String> getStatesWithTransitionsTo(Set<String> children) {
        Set<String> parents = new HashSet<>();
        children.forEach(child -> {
            if (incomingTransitions.containsKey(child))
                parents.addAll(incomingTransitions.get(child));
            if (incomingTransitionsToPREVIOUS.containsKey(child))
                parents.addAll(incomingTransitionsToPREVIOUS.get(child));
        });
        return parents;
    }

    public Set<String> getStatesWithTransitionsFrom(String... parents) {
        return getStatesWithTransitionsFrom(new HashSet<>(Arrays.asList(parents.clone())));
    }

    public Set<String> getStatesWithTransitionsFrom(Set<String> parents) {
        Set<String> children = new HashSet<>();
        parents.forEach(parent -> {
            if (outgoingTransitions.get(parent) != null) children.addAll(outgoingTransitions.get(parent));
        });
        return children;
    }

    public Map<String, Set<String>> getIncomingTransitionsWithHiddenTransitions() {
        Map<String, Set<String>> allIncoming = new HashMap<>();
        allIncoming.putAll(incomingTransitions);
        incomingTransitionsToPREVIOUS.forEach((stateName, stateNames) -> {
            if (allIncoming.containsKey(stateName)) allIncoming.get(stateName).addAll(stateNames);
            else allIncoming.put(stateName, stateNames);
        });
        return allIncoming;
    }

    public Map<String, Set<String>> getOutgoingTransitions() {
        return outgoingTransitions;
    }
}
