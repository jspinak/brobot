package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.primatives.enums.StateEnum;
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

    Map<StateEnum, Set<StateEnum>> incomingTransitions = new HashMap<>();
    Map<StateEnum, Set<StateEnum>> outgoingTransitions = new HashMap<>();
    Map<StateEnum, Set<StateEnum>> incomingTransitionsToPREVIOUS = new HashMap<>(); // updated dynamically

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

    public void add(StateEnum to, StateEnum from) {
        if (from != StateMemory.Enum.PREVIOUS) addIncomingTransition(to, from);
        addOutgoingTransition(to, from);
    }

    private void addIncomingTransition(StateEnum child, StateEnum parentToAdd) {
        if (incomingTransitions.containsKey(child)) incomingTransitions.get(child).add(parentToAdd);
        else {
            Set<StateEnum> stateEnums = new HashSet<>();
            stateEnums.add(parentToAdd);
            incomingTransitions.put(child, stateEnums);
        }
    }

    private void addOutgoingTransition(StateEnum childToAdd, StateEnum parent) {
        if (outgoingTransitions.containsKey(parent)) outgoingTransitions.get(parent).add(childToAdd);
        else {
            Set<StateEnum> stateEnums = new HashSet<>();
            stateEnums.add(childToAdd);
            outgoingTransitions.put(parent, stateEnums);
        }
    }

    public Set<StateEnum> getStatesWithTransitionsTo(StateEnum... children) {
        return getStatesWithTransitionsTo(new HashSet<>(Arrays.asList(children.clone())));
    }

    public Set<StateEnum> getStatesWithTransitionsTo(Set<StateEnum> children) {
        Set<StateEnum> parents = new HashSet<>();
        children.forEach(child -> {
            if (incomingTransitions.containsKey(child))
                parents.addAll(incomingTransitions.get(child));
            if (incomingTransitionsToPREVIOUS.containsKey(child))
                parents.addAll(incomingTransitionsToPREVIOUS.get(child));
        });
        return parents;
    }

    public Set<StateEnum> getStatesWithTransitionsFrom(StateEnum... parents) {
        return getStatesWithTransitionsFrom(new HashSet<>(Arrays.asList(parents.clone())));
    }

    public Set<StateEnum> getStatesWithTransitionsFrom(Set<StateEnum> parents) {
        Set<StateEnum> children = new HashSet<>();
        parents.forEach(parent -> {
            if (outgoingTransitions.get(parent) != null) children.addAll(outgoingTransitions.get(parent));
        });
        return children;
    }

    public Map<StateEnum, Set<StateEnum>> getIncomingTransitionsWithHiddenTransitions() {
        Map<StateEnum, Set<StateEnum>> allIncoming = new HashMap<>();
        allIncoming.putAll(incomingTransitions);
        incomingTransitionsToPREVIOUS.forEach((stateEnum, stateEnums) -> {
            if (allIncoming.containsKey(stateEnum)) allIncoming.get(stateEnum).addAll(stateEnums);
            else allIncoming.put(stateEnum, stateEnums);
        });
        return allIncoming;
    }

    public Map<StateEnum, Set<StateEnum>> getOutgoingTransitions() {
        return outgoingTransitions;
    }
}
