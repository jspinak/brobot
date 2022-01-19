package io.github.jspinak.brobot.services;

import io.github.jspinak.brobot.manageStates.StateTransitions;
import io.github.jspinak.brobot.manageStates.StateTransitionsJointTable;
import io.github.jspinak.brobot.primatives.enums.StateEnum;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Manages the StateTransitions repository and retrieves
 * StateTransitions given a StateEnum.
 */
@Component
public class StateTransitionsRepository {

    private StateTransitionsJointTable stateTransitionsJointTable;
    private Map<StateEnum, StateTransitions> repo = new HashMap<>();

    public StateTransitionsRepository(StateTransitionsJointTable stateTransitionsJointTable) {
        this.stateTransitionsJointTable = stateTransitionsJointTable;
    }

    public void add(StateTransitions stateTransitions) {
        for (StateEnum child : stateTransitions.getTransitions().keySet()) {
            stateTransitionsJointTable.add(child, stateTransitions.getStateName());
        }
        repo.put(stateTransitions.getStateName(), stateTransitions);
    }

    public Optional<StateTransitions> get(StateEnum stateEnum) {
        return Optional.ofNullable(repo.get(stateEnum));
    }

    public Set<StateEnum> getAllStates() {
        return repo.keySet();
    }

}
