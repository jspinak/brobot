package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.primatives.enums.SpecialStateType;
import lombok.Getter;
import org.python.antlr.ast.Str;
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

    public void emptyRepos() {
        incomingTransitions = new HashMap<>();
        outgoingTransitions = new HashMap<>();
        incomingTransitionsToPREVIOUS = new HashMap<>();
    }

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
        exitedState.getHiddenStateIds().forEach(hiddenState -> {
            if (incomingTransitionsToPREVIOUS.containsKey(hiddenState))
                incomingTransitionsToPREVIOUS.get(hiddenState).remove(exitedState.getId());
        });
    }

    /**
     * Each transition can activate multiple states. All pairs of (originating state/activated state)
     * are added to the joint table.
     */
    public void addToJointTable(StateTransitions stateTransitions) {
        for (IStateTransition transition : stateTransitions.getTransitions()) {
            for (Long child : transition.getActivate()) {
                add(child, stateTransitions.getStateId());
            }
        }
    }

    public void add(Long to, Long from) {
        if (!from.equals(SpecialStateType.PREVIOUS.getId()))
            addIncomingTransition(to, from);
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
        System.out.println("Getting states with transitions to: " + Arrays.toString(children));
        Set<Long> parents = new HashSet<>();
        for (Long child : children) {
            if (incomingTransitions.containsKey(child)) {
                parents.addAll(incomingTransitions.get(child));
                System.out.println("Found incoming transitions for " + child + ": " + incomingTransitions.get(child));
            } else {
                System.out.println("No incoming transitions found for " + child);
            }
            if (incomingTransitionsToPREVIOUS.containsKey(child)) {
                parents.addAll(incomingTransitionsToPREVIOUS.get(child));
                System.out.println("Found incoming PREVIOUS transitions for " + child + ": " + incomingTransitionsToPREVIOUS.get(child));
            } else {
                System.out.println("No incoming PREVIOUS transitions found for " + child);
            }
        }
        System.out.println("Returning parent states: " + parents);
        return parents;
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
        Map<Long, Set<Long>> allIncoming = new HashMap<>(incomingTransitions);
        incomingTransitionsToPREVIOUS.forEach((stateName, stateNames) -> {
            if (allIncoming.containsKey(stateName)) allIncoming.get(stateName).addAll(stateNames);
            else allIncoming.put(stateName, stateNames);
        });
        return allIncoming;
    }

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
