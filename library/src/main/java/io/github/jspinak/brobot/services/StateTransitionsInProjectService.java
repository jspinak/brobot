package io.github.jspinak.brobot.services;

import io.github.jspinak.brobot.manageStates.IStateTransition;
import io.github.jspinak.brobot.manageStates.StateTransitions;
import io.github.jspinak.brobot.manageStates.StateTransitionsJointTable;
import io.github.jspinak.brobot.primatives.enums.SpecialStateType;
import io.github.jspinak.brobot.report.Report;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Finds the correct Transition from one State to another,
 * taking into account the hidden States.
 */
@Component
@Getter
public class StateTransitionsInProjectService {

    private final StateTransitionsRepository stateTransitionsRepository;
    private final StateTransitionsJointTable stateTransitionsJointTable;

    private final Set<Long> statesToActivate = new HashSet<>();

    public StateTransitionsInProjectService(StateTransitionsRepository stateTransitionsRepository,
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

    public void setupRepo() {
        stateTransitionsRepository.populateStateTransitionsJointTable();
    }

    public List<IStateTransition> getAllIndividualTransitions() {
        return stateTransitionsRepository.getAllTransitions();
    }

    /**
     * Finds the correct stateName for the Transition. This may be PREVIOUS in the case of a hidden State.
     * @param from the state we are in
     * @param to the state we want to go to
     * @return either the stateName passed as a parameter, PREVIOUS, or UNKNOWN
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

    public Optional<StateTransitions> getTransitions(Long stateId) {
        return stateTransitionsRepository.get(stateId);
    }

    public Optional<IStateTransition> getTransition(Long fromState, Long toState) {
        Optional<StateTransitions> transitions = getTransitions(fromState);
        if (transitions.isEmpty()) return Optional.empty();
        return transitions.get().getTransitionFunctionByActivatedStateId(toState);
    }

    public void resetTimesSuccessful() {
        stateTransitionsRepository.getAllTransitions().forEach(transition ->
                transition.setTimesSuccessful(0));
    }

    public void printAllTransitions() {
        Report.print("State Transitions in Project:\n");
        stateTransitionsRepository.getAllTransitions().forEach(transition ->
                Report.println(transition.toString()));
        Report.println();
    }

}
