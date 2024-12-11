package io.github.jspinak.brobot.testingAUTs;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.log.entities.LogEntry;
import io.github.jspinak.brobot.logging.ActionLogger;
import io.github.jspinak.brobot.logging.AutomationSession;
import io.github.jspinak.brobot.logging.LogUpdateSender;
import io.github.jspinak.brobot.manageStates.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class StateTraversalService {

    private final AdjacentStates adjacentStates;
    private final AllStatesInProjectService allStatesInProjectService;
    private final UnvisitedStates unvisitedStates;
    private final StateTransitionsManagement stateTransitionsManagement;
    private final InitialStates initialStates;
    private final StateMemory stateMemory;
    private final LogUpdateSender logUpdateSender;
    private final ActionLogger actionLogger;
    private final AutomationSession automationSession;

    private Set<Long> visitedStates = new HashSet<>();
    private Set<Long> unvisitedStateSet = new HashSet<>();
    private Set<Long> unreachableStates = new HashSet<>();

    public StateTraversalService(AdjacentStates adjacentStates, AllStatesInProjectService allStatesInProjectService,
                                 UnvisitedStates unvisitedStates, StateMemory stateMemory,
                                 StateTransitionsManagement stateTransitionsManagement,
                                 InitialStates initialStates, LogUpdateSender logUpdateSender,
                                 ActionLogger actionLogger, AutomationSession automationSession) {
        this.adjacentStates = adjacentStates;
        this.allStatesInProjectService = allStatesInProjectService;
        this.unvisitedStates = unvisitedStates;
        this.stateTransitionsManagement = stateTransitionsManagement;
        this.initialStates = initialStates;
        this.stateMemory = stateMemory;
        this.logUpdateSender = logUpdateSender;
        this.actionLogger = actionLogger;
        this.automationSession = automationSession;
    }

    /**
     * Looks for adjacent states that have not been visited. If no unvisited, adjacent states
     * are available, it returns unvisited states that are farther away.
     * @return states to visit
     */
    public Set<Long> getAdjacentUnvisited(Set<Long> visitedStates) {
        Set<Long> adjacentUnvisited = new HashSet<>();
        Set<Long> adjacent = adjacentStates.getAdjacentStates();
        adjacent.forEach(stateId -> {
            Optional<State> stateOpt = allStatesInProjectService.getState(stateId);
            stateOpt.ifPresent(state -> {
                if (!visitedStates.contains(state.getId())) adjacentUnvisited.add(stateId);
            });
        });
        return adjacentUnvisited;
    }

    public Set<Long> traverseAllStates() {
        // Reset state at start of each traversal
        stateMemory.removeAllStates();
        visitedStates = new HashSet<>();
        unvisitedStateSet = new HashSet<>();
        unreachableStates = new HashSet<>();

        System.out.println("traversing all states");
        unvisitedStateSet = new HashSet<>(allStatesInProjectService.getAllStateIds());
        System.out.println("unvisited states = " + unvisitedStateSet);
        int failedAttempt = 0;
        initialStates.findIntialStates();
        visitedStates = new HashSet<>(stateMemory.getActiveStates());
        visitedStates.forEach(unvisitedStateSet::remove);
        Optional<Long> closestUnvisitedState = getNextState();
        while (closestUnvisitedState.isPresent() && failedAttempt < 10 && !unvisitedStateSet.isEmpty()) {
            Long closestStateId = closestUnvisitedState.get();
            System.out.println("closest unvisited = " + closestStateId);
            System.out.println("visited states = " + visitedStates);
            StringBuilder unvisitedStatesString = new StringBuilder();
            unvisitedStateSet.forEach(unvisitedStatesString::append);
            LogEntry logEntry = actionLogger.logObservation(automationSession.getCurrentSessionId(),
                    "unvisited: failed attempts = " + failedAttempt, unvisitedStatesString.toString(), "info");
            logUpdateSender.sendLogUpdate(Collections.singletonList(logEntry));

            if (stateTransitionsManagement.openState(closestStateId)) {
                visitedStates.add(closestStateId);
            } else {
                failedAttempt++;
                unreachableStates.add(closestStateId);
            }
            unvisitedStateSet.remove(closestStateId);
            closestUnvisitedState = getNextState();
        }
        System.out.println("finished traversing all states");
        return visitedStates;
    }

    private Optional<Long> getNextState() {
        Optional<Long> closestUnvisitedState = getAdjacentUnvisited(visitedStates).stream().findFirst();
        if (closestUnvisitedState.isPresent()) return closestUnvisitedState;
        return unvisitedStates.getClosestUnvisited();
    }
}
