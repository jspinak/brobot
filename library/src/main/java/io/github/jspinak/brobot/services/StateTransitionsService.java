package io.github.jspinak.brobot.services;

import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.manageStates.StateTransition;
import io.github.jspinak.brobot.manageStates.StateTransitions;
import io.github.jspinak.brobot.manageStates.StateTransitionsJointTable;
import io.github.jspinak.brobot.primatives.enums.StateEnum;
import io.github.jspinak.brobot.manageStates.StateMemory;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static io.github.jspinak.brobot.datatypes.state.NullState.Name.NULL;
import static io.github.jspinak.brobot.manageStates.StateMemory.Enum.PREVIOUS;

/**
 * Finds the correct Transition from one State to another,
 * taking into account the hidden States.
 */
@Component
@Getter
public class StateTransitionsService {

    private StateTransitionsRepository stateTransitionsRepository;
    private StateTransitionsJointTable stateTransitionsJointTable;
    private StateService stateService;

    private Set<StateEnum> statesToActivate = new HashSet<>();

    public StateTransitionsService(StateTransitionsRepository stateTransitionsRepository,
                                   StateTransitionsJointTable stateTransitionsJointTable,
                                   StateService stateService) {
        this.stateTransitionsRepository = stateTransitionsRepository;
        this.stateTransitionsJointTable = stateTransitionsJointTable;
        this.stateService = stateService;
    }

    /**
     * Finds the correct StateEnum for the Transition. This may be PREVIOUS in the case of a hidden State.
     * @param from StateEnum
     * @param to StateEnum
     * @return either the StateEnum passed as a parameter, PREVIOUS, or UNKNOWN
     */
    public StateEnum getTransitionToEnum(StateEnum from, StateEnum to) {
        // check first if it is a normal transition
        if (stateTransitionsJointTable.getStatesWithTransitionsFrom(from).contains(to)) return to;
        // if not, it may be a hidden state transition
        if (!stateTransitionsJointTable.getIncomingTransitionsToPREVIOUS().containsKey(to) ||
            !stateTransitionsJointTable.getIncomingTransitionsToPREVIOUS().get(to).contains(from))
            return NULL; // it is not a hidden state transition either
        return PREVIOUS; // it is a hidden state transition
    }

    public Optional<StateTransitions> getTransitions(StateEnum stateEnum) {
        return stateTransitionsRepository.get(stateEnum);
    }

    public Optional<StateTransition> getTransition(StateEnum fromState, StateEnum toState) {
        Optional<StateTransitions> transitions = getTransitions(fromState);
        if (transitions.isEmpty()) return Optional.empty();
        return transitions.get().getStateTransition(toState);
    }

    public void resetTimesSuccessful() {
        stateTransitionsRepository.getAllTransitions().forEach(transition ->
                transition.setTimesSuccessful(0));
    }

}
