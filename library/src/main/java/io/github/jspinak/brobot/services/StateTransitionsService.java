package io.github.jspinak.brobot.services;

import io.github.jspinak.brobot.manageStates.StateTransition;
import io.github.jspinak.brobot.manageStates.StateTransitions;
import io.github.jspinak.brobot.manageStates.StateTransitionsJointTable;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Finds the correct Transition from one State to another,
 * taking into account the hidden States.
 */
@Component
@Getter
public class StateTransitionsService {

    private final StateTransitionsRepository stateTransitionsRepository;
    private final StateTransitionsJointTable stateTransitionsJointTable;

    private final Set<String> statesToActivate = new HashSet<>();

    public StateTransitionsService(StateTransitionsRepository stateTransitionsRepository,
                                   StateTransitionsJointTable stateTransitionsJointTable) {
        this.stateTransitionsRepository = stateTransitionsRepository;
        this.stateTransitionsJointTable = stateTransitionsJointTable;
    }

    /**
     * Finds the correct stateName for the Transition. This may be PREVIOUS in the case of a hidden State.
     * @param from the state we are in
     * @param to the state we want to go to
     * @return either the stateName passed as a parameter, PREVIOUS, or UNKNOWN
     */
    public String getTransitionToEnum(String from, String to) {
        // check first if it is a normal transition
        if (stateTransitionsJointTable.getStatesWithTransitionsFrom(from).contains(to)) return to;
        // if not, it may be a hidden state transition
        if (!stateTransitionsJointTable.getIncomingTransitionsToPREVIOUS().containsKey(to) ||
            !stateTransitionsJointTable.getIncomingTransitionsToPREVIOUS().get(to).contains(from))
            return "NULL"; // it is not a hidden state transition either
        return "PREVIOUS"; // it is a hidden state transition
    }

    public Optional<StateTransitions> getTransitions(String stateName) {
        return stateTransitionsRepository.get(stateName);
    }

    public Optional<StateTransition> getTransition(String fromState, String toState) {
        Optional<StateTransitions> transitions = getTransitions(fromState);
        if (transitions.isEmpty()) return Optional.empty();
        return transitions.get().getStateTransition(toState);
    }

    public void resetTimesSuccessful() {
        stateTransitionsRepository.getAllTransitions().forEach(transition ->
                transition.setTimesSuccessful(0));
    }

}
