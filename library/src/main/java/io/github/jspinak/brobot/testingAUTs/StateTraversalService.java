package io.github.jspinak.brobot.testingAUTs;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.log.entities.LogEntry;
import io.github.jspinak.brobot.logging.ActionLogger;
import io.github.jspinak.brobot.logging.AutomationSession;
import io.github.jspinak.brobot.manageStates.AdjacentStates;
import io.github.jspinak.brobot.manageStates.InitialStates;
import io.github.jspinak.brobot.manageStates.StateMemory;
import io.github.jspinak.brobot.manageStates.StateTransitionsManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class StateTraversalService {
    private static final Logger logger = LoggerFactory.getLogger(StateTraversalService.class);

    private final AdjacentStates adjacentStates;
    private final AllStatesInProjectService allStatesInProjectService;
    private final UnvisitedStates unvisitedStates;
    private final StateTransitionsManagement stateTransitionsManagement;
    private final InitialStates initialStates;
    private final StateMemory stateMemory;
    private final ActionLogger actionLogger;
    private final AutomationSession automationSession;
    private final VisitAllStateImages visitAllStateImages;

    private final List<StateVisit> stateVisits = new ArrayList<>();
    private Set<Long> visitedStates = new HashSet<>();
    private Set<Long> unvisitedStateSet = new HashSet<>();
    private Set<Long> unreachableStates = new HashSet<>();

    public StateTraversalService(AdjacentStates adjacentStates, AllStatesInProjectService allStatesInProjectService,
                                 UnvisitedStates unvisitedStates, StateMemory stateMemory,
                                 StateTransitionsManagement stateTransitionsManagement,
                                 InitialStates initialStates, ActionLogger actionLogger, AutomationSession automationSession,
                                 VisitAllStateImages visitAllStateImages) {
        this.adjacentStates = adjacentStates;
        this.allStatesInProjectService = allStatesInProjectService;
        this.unvisitedStates = unvisitedStates;
        this.stateTransitionsManagement = stateTransitionsManagement;
        this.initialStates = initialStates;
        this.stateMemory = stateMemory;
        this.actionLogger = actionLogger;
        this.automationSession = automationSession;
        this.visitAllStateImages = visitAllStateImages;
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

    public Set<Long> traverseAllStates(boolean visitAllImages) {
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

        // Record initial states
        stateMemory.getActiveStates().forEach(stateId -> {
            Optional<State> state = allStatesInProjectService.getState(stateId);
            state.ifPresent(s -> {
                stateVisits.add(new StateVisit(stateId, s.getName(), true));
            });
        });

        actionLogger.logObservation(automationSession.getCurrentSessionId(),
                "Initial states:", stateMemory.getActiveStateNamesAsString(), "info");
        visitedStates = new HashSet<>(stateMemory.getActiveStates());
        visitedStates.forEach(initialState -> {
            unvisitedStateSet.remove(initialState);
            allStatesInProjectService.getState(initialState).ifPresent(s -> {
                if (visitAllImages) visitAllStateImages.visitAllStateImages(s);
            });
        });
        Optional<Long> closestUnvisitedState = getNextState();
        while (closestUnvisitedState.isPresent() && failedAttempt < 10 && !unvisitedStateSet.isEmpty()) {
            Long closestStateId = closestUnvisitedState.get();
            System.out.println("closest unvisited = " + closestStateId);
            System.out.println("visited states = " + visitedStates);
            StringBuilder unvisitedStatesString = new StringBuilder();
            unvisitedStateSet.forEach(unvisitedStatesString::append);
            if (failedAttempt > 0) actionLogger.logObservation(automationSession.getCurrentSessionId(),
                    "unvisited: failed attempts = " + failedAttempt, unvisitedStatesString.toString(), "info");

            Optional<State> state = allStatesInProjectService.getState(closestStateId);
            boolean success = stateTransitionsManagement.openState(closestStateId);
            state.ifPresent(s -> stateVisits.add(new StateVisit(closestStateId, s.getName(), success)));

            if (success) {
                visitedStates.add(closestStateId);
                unreachableStates.remove(closestStateId);
                state.ifPresent(s -> {
                    if (visitAllImages) visitAllStateImages.visitAllStateImages(s);
                });
            } else {
                failedAttempt++;
                unreachableStates.add(closestStateId); // this is a temporary solution since the state may be reachable from another state
            }
            unvisitedStateSet.remove(closestStateId);
            closestUnvisitedState = getNextState();
        }
        System.out.println("finished traversing all states");
        logTraversalSummary();
        return visitedStates;
    }

    private Optional<Long> getNextState() {
        Optional<Long> closestUnvisitedState = getAdjacentUnvisited(visitedStates).stream().findFirst();
        if (closestUnvisitedState.isPresent()) return closestUnvisitedState;
        return unvisitedStates.getClosestUnvisited();
    }

    private void logTraversalSummary() {
        StringBuilder summary = new StringBuilder();

        summary.append("\nStates visited in order:\n");
        for (StateVisit visit : stateVisits) {
            summary.append(String.format("- %s (ID: %d) - %s\n",
                    visit.getStateName(),
                    visit.getStateId(),
                    visit.isSuccessful() ? "Success" : "Failed"));
        }

        summary.append("\nSuccessfully visited states:\n");
        stateVisits.stream()
                .filter(StateVisit::isSuccessful)
                .forEach(visit -> summary.append(String.format("- %s (ID: %d)\n", visit.getStateName(), visit.getStateId())));

        summary.append("\nUnreachable states:\n");
        unreachableStates.forEach(stateId -> {
            Optional<State> state = allStatesInProjectService.getState(stateId);
            state.ifPresent(s -> summary.append(String.format("- %s (ID: %d)\n", s.getName(), s.getId())));
        });

        logger.info("State Traversal Summary:");
        logger.info(summary.toString());
        actionLogger.logObservation(automationSession.getCurrentSessionId(), "State Traversal Summary:", summary.toString(), "info");
    }

    // Getters for traversal results
    public List<StateVisit> getStateVisits() {
        return new ArrayList<>(stateVisits);
    }

    public Set<Long> getUnreachableStates() {
        return new HashSet<>(unreachableStates);
    }

    public Set<Long> getSuccessfullyVisitedStates() {
        return new HashSet<>(visitedStates);
    }
}
