package io.github.jspinak.brobot.testingAUTs;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.manageStates.AdjacentStates;
import io.github.jspinak.brobot.manageStates.StateTransitionsManagement;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
public class StateTraversalService {

    private final AdjacentStates adjacentStates;
    private final AllStatesInProjectService allStatesInProjectService;
    private final UnvisitedStates unvisitedStates;
    private final StateTransitionsManagement stateTransitionsManagement;

    public StateTraversalService(AdjacentStates adjacentStates, AllStatesInProjectService allStatesInProjectService,
                                 UnvisitedStates unvisitedStates,
                                 StateTransitionsManagement stateTransitionsManagement) {
        this.adjacentStates = adjacentStates;
        this.allStatesInProjectService = allStatesInProjectService;
        this.unvisitedStates = unvisitedStates;
        this.stateTransitionsManagement = stateTransitionsManagement;
    }

    /**
     * Looks for adjacent states that have not been visited. If no unvisited, adjacent states
     * are available, it returns unvisited states that are farther away.
     * @return states to visit
     */
    public Set<Long> getAdjacentUnvisited() {
        Set<Long> adjacentUnvisited = new HashSet<>();
        Set<Long> adjacent = adjacentStates.getAdjacentStates();
        adjacent.forEach(stateName -> {
            Optional<State> stateOpt = allStatesInProjectService.getState(stateName);
            stateOpt.ifPresent(state -> {
                if (state.getTimesVisited() == 0) adjacentUnvisited.add(stateName);
            });
        });
        return adjacentUnvisited;
    }

    public void traverseAllStates() {
        Optional<Long> closestUnvisitedState = getNextState();
        while (closestUnvisitedState.isPresent()) {
            stateTransitionsManagement.openState(closestUnvisitedState.get());
            closestUnvisitedState = getNextState();
        }
    }

    private Optional<Long> getNextState() {
        Optional<Long> closestUnvisitedState = getAdjacentUnvisited().stream().findFirst();
        if (closestUnvisitedState.isPresent()) return closestUnvisitedState;
        return unvisitedStates.getClosestUnvisited();
    }
}
