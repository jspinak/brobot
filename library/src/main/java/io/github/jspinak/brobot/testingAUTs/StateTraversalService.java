package io.github.jspinak.brobot.testingAUTs;

import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.manageStates.AdjacentStates;
import io.github.jspinak.brobot.manageStates.StateTransitionsManagement;
import io.github.jspinak.brobot.primatives.enums.StateEnum;
import io.github.jspinak.brobot.services.StateService;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class StateTraversalService {

    private AdjacentStates adjacentStates;
    private StateService stateService;
    private UnvisitedStates unvisitedStates;
    private StateTransitionsManagement stateTransitionsManagement;

    public StateTraversalService(AdjacentStates adjacentStates, StateService stateService,
                                 UnvisitedStates unvisitedStates,
                                 StateTransitionsManagement stateTransitionsManagement) {
        this.adjacentStates = adjacentStates;
        this.stateService = stateService;
        this.unvisitedStates = unvisitedStates;
        this.stateTransitionsManagement = stateTransitionsManagement;
    }

    /**
     * Looks for adjacent states that have not been visited. If no unvisited, adjacent states
     * are available, it returns unvisited states that are farther away.
     * @return states to visit
     */
    public Set<StateEnum> getAdjacentUnvisited() {
        Set<StateEnum> adjacentUnvisited = new HashSet<>();
        Set<StateEnum> adjacent = adjacentStates.getAdjacentStates();
        adjacent.forEach(stateName -> {
            Optional<State> stateOpt = stateService.findByName(stateName);
            stateOpt.ifPresent(state -> {
                if (state.getTimesVisited() == 0) adjacentUnvisited.add(stateName);
            });
        });
        return adjacentUnvisited;
    }

    public void traverseAllStates() {
        Optional<StateEnum> closestUnvisitedState = getNextState();
        while (closestUnvisitedState.isPresent()) {
            stateTransitionsManagement.openState(closestUnvisitedState.get());
            closestUnvisitedState = getNextState();
        }
    }

    private Optional<StateEnum> getNextState() {
        Optional<StateEnum> closestUnvisitedState = getAdjacentUnvisited().stream().findFirst();
        if (closestUnvisitedState.isPresent()) return closestUnvisitedState;
        return unvisitedStates.getClosestUnvisited();
    }
}
