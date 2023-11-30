package io.github.jspinak.brobot.services;

import io.github.jspinak.brobot.manageStates.StateTransition;
import io.github.jspinak.brobot.manageStates.StateTransitions;
import io.github.jspinak.brobot.manageStates.StateTransitionsJointTable;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Manages the StateTransitions repository and retrieves
 * StateTransitions given a state name.
 */
@Component
public class StateTransitionsRepository {

    private StateTransitionsJointTable stateTransitionsJointTable;
    private Map<String, StateTransitions> repo = new HashMap<>();

    public StateTransitionsRepository(StateTransitionsJointTable stateTransitionsJointTable) {
        this.stateTransitionsJointTable = stateTransitionsJointTable;
    }

    public void add(StateTransitions stateTransitions) {
        for (String child : stateTransitions.getTransitions().keySet()) {
            stateTransitionsJointTable.add(child, stateTransitions.getStateName());
        }
        repo.put(stateTransitions.getStateName(), stateTransitions);
    }

    public Optional<StateTransitions> get(String stateName) {
        return Optional.ofNullable(repo.get(stateName));
    }

    public Set<String> getAllStates() {
        return repo.keySet();
    }

    /**
     * Returns all StateTransition objects, including ToTransitions.
     * @return a list of StateTransition objects in the model.
     */
    public List<StateTransition> getAllTransitions() {
        List<StateTransition> allTransitions = new ArrayList<>();
        repo.values().forEach(trs -> {
            allTransitions.addAll(trs.getTransitions().values());
            allTransitions.add(trs.getTransitionFinish());
        });
        return allTransitions;
    }

}
