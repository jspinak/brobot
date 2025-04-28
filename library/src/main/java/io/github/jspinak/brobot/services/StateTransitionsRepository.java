package io.github.jspinak.brobot.services;

import io.github.jspinak.brobot.manageStates.IStateTransition;
import io.github.jspinak.brobot.manageStates.StateTransitions;
import io.github.jspinak.brobot.manageStates.StateTransitionsJointTable;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Manages the StateTransitions repository and retrieves
 * StateTransitions given a state name.
 */
@Component
@Getter
public class StateTransitionsRepository {

    private List<StateTransitions> repo = new ArrayList<>();
    private final StateTransitionsJointTable stateTransitionsJointTable;

    public StateTransitionsRepository(StateTransitionsJointTable stateTransitionsJointTable) {
        this.stateTransitionsJointTable = stateTransitionsJointTable;
    }

    /*
     * After creating a StateTransitions class manually with code, the state
     * references need to be converted from names to ids. This can only happen when all states have
     * been given ids. This method is likely called before all states have been initialized
     * with ids. The method stateManagementService.convertAllStateTransitions is called in
     * another class.
     */
    public void add(StateTransitions stateTransitions) {
        repo.add(stateTransitions);
    }

    /**
     * This method is called after all states have been initialized with ids.
     * Each transition can activate multiple states. All pairs of (originating state/activated state)
     * are added to the joint table.
     */
    public void populateStateTransitionsJointTable() {
        repo.forEach(stateTransitionsJointTable::addToJointTable);
    }

    /**
     * Returns a StateTransitions object given a state id.
     *
     * @param stateId the id of the state to find.
     * @return an Optional containing the StateTransitions object if found, or an empty Optional if not found.
     */
    public Optional<StateTransitions> get(Long stateId) {
        if (stateId == null) return Optional.empty();
        return repo.stream()
                .filter(transitions -> Objects.equals(stateId, transitions.getStateId()))
                .findFirst();
    }

    /**
     * Returns a set of all StateTransitions objects in the repository with state ids.
     *
     * @return a set of state ids for all StateTransitions with ids.
     */
    public Set<Long> getAllStateIds() {
        return repo.stream()
                .map(StateTransitions::getStateId)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Returns all StateTransition objects, including ToTransitions.
     * 
     * @return a list of StateTransition objects in the model.
     */
    public List<IStateTransition> getAllTransitions() {
        List<IStateTransition> allTransitions = new ArrayList<>();
        repo.forEach(trs -> {
            allTransitions.addAll(trs.getTransitions());
            allTransitions.add(trs.getTransitionFinish());
        });
        return allTransitions;
    }

    public List<StateTransitions> getAllStateTransitionsAsCopy() {
        return new ArrayList<>(repo);
    }

    public void emptyRepos() {
        repo = new ArrayList<>();
        stateTransitionsJointTable.emptyRepos();
    }

    public void print() {
        System.out.println("StateTransitionsRepository: ");
        repo.forEach(stateTransitions -> {
            System.out.println(stateTransitions.toString());
        });
    }

}
