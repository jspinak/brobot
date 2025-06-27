package io.github.jspinak.brobot.tools.testing.exploration;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.transition.StateNavigator;
import io.github.jspinak.brobot.statemanagement.AdjacentStates;
import io.github.jspinak.brobot.statemanagement.InitialStates;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.tools.logging.ActionLogger;
import io.github.jspinak.brobot.tools.logging.ExecutionSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Orchestrates comprehensive state exploration for automated testing of the application.
 * 
 * <p>StateTraversalService is the core engine of the testing exploration framework,
 * responsible for systematically visiting all states in the application to ensure
 * complete test coverage. It implements an intelligent traversal algorithm that
 * prioritizes efficiency while guaranteeing comprehensive coverage.</p>
 * 
 * <h2>Traversal Strategy</h2>
 * <p>The service employs a hybrid approach combining:</p>
 * <ol>
 *   <li><b>Adjacent-First Strategy</b> - Prioritizes states directly reachable from
 *       the current position to minimize transition overhead</li>
 *   <li><b>Shortest-Path Fallback</b> - When no adjacent unvisited states exist,
 *       finds the closest unvisited state using path-finding algorithms</li>
 *   <li><b>Failure Resilience</b> - Tracks unreachable states and continues
 *       exploration up to a configurable failure threshold</li>
 * </ol>
 * 
 * <h2>State Visit Tracking</h2>
 * <p>The service maintains comprehensive records of:</p>
 * <ul>
 *   <li>Successfully visited states</li>
 *   <li>Failed visit attempts with reasons</li>
 *   <li>Unreachable states (may be temporary based on current position)</li>
 *   <li>Visit order and timing for analysis</li>
 * </ul>
 * 
 * <h2>Image Verification</h2>
 * <p>When enabled, the service can verify all images within each visited state,
 * ensuring visual elements are properly detected. This is controlled by the
 * visitAllImages parameter in {@link #traverseAllStates(boolean)}.</p>
 * 
 * <h2>Failure Handling</h2>
 * <p>The service implements intelligent failure handling:</p>
 * <ul>
 *   <li>Continues exploration after individual state failures</li>
 *   <li>Tracks unreachable states for later analysis</li>
 *   <li>Stops after 10 consecutive failures to prevent infinite loops</li>
 *   <li>Logs detailed information for debugging navigation issues</li>
 * </ul>
 * 
 * <h2>Integration with Framework Components</h2>
 * <ul>
 *   <li>{@link StateExplorationTracker} - Identifies and prioritizes unvisited states</li>
 *   <li>{@link StateImageValidator} - Verifies visual elements within states</li>
 *   <li>{@link StateVisit} - Records individual visit attempts</li>
 *   <li>{@link AdjacentStates} - Provides information about neighboring states</li>
 *   <li>{@link StateNavigator} - Executes state navigation</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Perform comprehensive state traversal with image verification
 * Set<Long> visitedStates = stateTraversalService.traverseAllStates(true);
 * 
 * // Get detailed results
 * List<StateVisit> visits = stateTraversalService.getStateVisits();
 * Set<Long> unreachable = stateTraversalService.getUnreachableStates();
 * 
 * // Generate report
 * logger.info("Visited {} of {} states", 
 *     visitedStates.size(), 
 *     allStates.size());
 * logger.info("Unreachable states: {}", unreachable);
 * }</pre>
 * 
 * @see StateExplorationTracker for unvisited state tracking
 * @see StateImageValidator for image verification
 * @see ExplorationOrchestrator for test orchestration
 * @see StateVisit for visit records
 * @author jspinak
 */
@Component
public class StateTraversalService {
    private static final Logger logger = LoggerFactory.getLogger(StateTraversalService.class);

    private final AdjacentStates adjacentStates;
    private final StateService allStatesInProjectService;
    private final StateExplorationTracker unvisitedStates;
    private final StateNavigator stateTransitionsManagement;
    private final InitialStates initialStates;
    private final StateMemory stateMemory;
    private final ActionLogger actionLogger;
    private final ExecutionSession automationSession;
    private final StateImageValidator visitAllStateImages;

    private final List<StateVisit> stateVisits = new ArrayList<>();
    private Set<Long> visitedStates = new HashSet<>();
    private Set<Long> unvisitedStateSet = new HashSet<>();
    private Set<Long> unreachableStates = new HashSet<>();

    public StateTraversalService(AdjacentStates adjacentStates, StateService allStatesInProjectService,
                                 StateExplorationTracker unvisitedStates, StateMemory stateMemory,
                                 StateNavigator stateTransitionsManagement,
                                 InitialStates initialStates, ActionLogger actionLogger, ExecutionSession automationSession,
                                 StateImageValidator visitAllStateImages) {
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
     * Identifies unvisited states that are adjacent to the current position.
     * 
     * <p>This method implements the adjacent-first strategy by finding states that:
     * <ul>
     *   <li>Are directly reachable from the current active states</li>
     *   <li>Have not been visited during the current traversal</li>
     *   <li>Are valid states in the project</li>
     * </ul>
     * 
     * <p>Adjacent states are preferred because they typically require fewer
     * transitions and are more likely to succeed, improving traversal efficiency.</p>
     * 
     * @param visitedStates the set of state IDs that have already been visited
     *                      during the current traversal session
     * @return a set of unvisited state IDs that are adjacent to the current position.
     *         Returns an empty set if no adjacent unvisited states exist.
     * @see AdjacentStates#getAdjacentStates() for how adjacency is determined
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

    /**
     * Performs comprehensive traversal of all states in the application.
     * 
     * <p>This is the main entry point for automated state exploration. The method
     * systematically visits all reachable states, optionally verifying images
     * within each state. The traversal continues until all states have been
     * visited or the failure threshold is reached.</p>
     * 
     * <h3>Traversal Process</h3>
     * <ol>
     *   <li>Reset traversal state and clear previous results</li>
     *   <li>Identify initial states as starting points</li>
     *   <li>Mark initial states as visited and optionally verify their images</li>
     *   <li>Iteratively visit unvisited states using the traversal strategy</li>
     *   <li>Track successes, failures, and unreachable states</li>
     *   <li>Log comprehensive summary upon completion</li>
     * </ol>
     * 
     * <h3>Failure Handling</h3>
     * <p>The traversal continues despite individual state failures but stops after
     * 10 consecutive failures to prevent infinite loops. States that fail to open
     * are marked as unreachable, though they may be reachable from other states.</p>
     * 
     * <h3>Image Verification</h3>
     * <p>When visitAllImages is true, the service verifies all StateImages within
     * each successfully visited state, ensuring comprehensive visual testing.</p>
     * 
     * @param visitAllImages if true, verifies all images within each visited state.
     *                       This adds visual element verification to state coverage.
     * @return the set of state IDs that were successfully visited during traversal.
     *         This may be less than the total number of states if some are unreachable.
     * @see #getNextState() for state selection logic
     * @see #logTraversalSummary() for result reporting
     * @see StateImageValidator#visitAllStateImages(State) for image verification
     */
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

    /**
     * Determines the next state to visit using the traversal strategy.
     * 
     * <p>Implements a two-tier selection strategy:</p>
     * <ol>
     *   <li><b>Adjacent Priority</b> - First checks for unvisited states that are
     *       adjacent to the current position, as these require minimal transitions</li>
     *   <li><b>Shortest Path</b> - If no adjacent unvisited states exist, uses
     *       path-finding to locate the closest unvisited state from any position</li>
     * </ol>
     * 
     * <p>This strategy optimizes traversal efficiency by minimizing the number
     * and complexity of transitions required to achieve complete coverage.</p>
     * 
     * @return an Optional containing the ID of the next state to visit,
     *         or empty if no reachable unvisited states remain
     * @see #getAdjacentUnvisited(Set) for adjacent state identification
     * @see StateExplorationTracker#getClosestUnvisited() for path-based selection
     */
    private Optional<Long> getNextState() {
        Optional<Long> closestUnvisitedState = getAdjacentUnvisited(visitedStates).stream().findFirst();
        if (closestUnvisitedState.isPresent()) return closestUnvisitedState;
        return unvisitedStates.getClosestUnvisited();
    }

    /**
     * Logs a comprehensive summary of the traversal results.
     * 
     * <p>Generates and logs a detailed report including:</p>
     * <ul>
     *   <li>Chronological list of all state visits with success/failure status</li>
     *   <li>List of successfully visited states</li>
     *   <li>List of unreachable states that couldn't be accessed</li>
     * </ul>
     * 
     * <p>This summary is crucial for:</p>
     * <ul>
     *   <li>Understanding test coverage achieved</li>
     *   <li>Identifying problematic states or transitions</li>
     *   <li>Debugging navigation failures</li>
     *   <li>Generating test reports</li>
     * </ul>
     * 
     * <p>The summary is logged both to the standard logger and the action logger
     * for comprehensive record-keeping and analysis.</p>
     */
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

    /**
     * Returns a copy of all state visits recorded during traversal.
     * 
     * <p>Provides access to the complete visit history including both
     * successful and failed attempts. This data is valuable for:</p>
     * <ul>
     *   <li>Analyzing traversal patterns</li>
     *   <li>Identifying failure patterns</li>
     *   <li>Generating detailed test reports</li>
     *   <li>Debugging navigation issues</li>
     * </ul>
     * 
     * @return a new list containing all StateVisit records in chronological order
     * @see StateVisit for the structure of visit records
     */
    public List<StateVisit> getStateVisits() {
        return new ArrayList<>(stateVisits);
    }

    /**
     * Returns a copy of states that could not be reached during traversal.
     * 
     * <p>Unreachable states may indicate:</p>
     * <ul>
     *   <li>Broken transitions in the state model</li>
     *   <li>Missing UI elements preventing navigation</li>
     *   <li>States only reachable from specific conditions</li>
     *   <li>Isolated states with no incoming transitions</li>
     * </ul>
     * 
     * <p>Note: A state marked as unreachable during one traversal might be
     * reachable from a different starting point or application state.</p>
     * 
     * @return a new set containing IDs of all unreachable states
     */
    public Set<Long> getUnreachableStates() {
        return new HashSet<>(unreachableStates);
    }

    /**
     * Returns a copy of states that were successfully visited during traversal.
     * 
     * <p>Successfully visited states represent the achieved test coverage.
     * This set can be used to:</p>
     * <ul>
     *   <li>Calculate coverage percentage</li>
     *   <li>Verify minimum coverage requirements</li>
     *   <li>Compare coverage across different test runs</li>
     * </ul>
     * 
     * @return a new set containing IDs of all successfully visited states
     */
    public Set<Long> getSuccessfullyVisitedStates() {
        return new HashSet<>(visitedStates);
    }
}
