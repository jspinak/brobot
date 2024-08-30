package io.github.jspinak.brobot.services;

import io.github.jspinak.brobot.manageStates.IStateTransition;
import io.github.jspinak.brobot.manageStates.StateManagementService;
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

    private List<StateTransitions> preliminaryRepo = new ArrayList<>();
    private Map<Long, StateTransitions> repo = new HashMap<>();
    private StateTransitionsJointTable stateTransitionsJointTable;

    public StateTransitionsRepository(StateTransitionsJointTable stateTransitionsJointTable) {
        this.stateTransitionsJointTable = stateTransitionsJointTable;
    }

    /*
    After creating a StateTransitions class manually with code, the state references need
    to be converted from names to ids. This can only happen when all states have been given
    ids. This method is likely called before all states have been initialized with ids.
    The method stateManagementService.convertAllStateTransitions is called in another class.
     */
    public void add(StateTransitions stateTransitions) {
        preliminaryRepo.add(stateTransitions);
    }

    public void populateRepoWithPreliminaryStateTransitions() {
        preliminaryRepo.forEach(stateTransitions -> {
            for (Long child : stateTransitions.getTransitions().keySet()) {
                stateTransitionsJointTable.add(child, stateTransitions.getStateId());
            }
            repo.put(stateTransitions.getStateId(), stateTransitions);
        });
    }

    public Optional<StateTransitions> get(Long stateId) {
        return Optional.ofNullable(repo.get(stateId));
    }

    public Set<Long> getAllStates() {
        return repo.keySet();
    }

    /**
     * Returns all StateTransition objects, including ToTransitions.
     * @return a list of StateTransition objects in the model.
     */
    public List<IStateTransition> getAllTransitions() {
        List<IStateTransition> allTransitions = new ArrayList<>();
        repo.values().forEach(trs -> {
            allTransitions.addAll(trs.getTransitions().values());
            allTransitions.add(trs.getTransitionFinish());
        });
        return allTransitions;
    }

    public List<StateTransitions> getAllStateTransitions() {
        return new ArrayList<>(repo.values());
    }

    public void emptyRepos() {
        preliminaryRepo = new ArrayList<>();
        repo = new HashMap<>();
        stateTransitionsJointTable.emptyRepos();
    }

}
