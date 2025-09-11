package io.github.jspinak.brobot.tools.testing.exploration;

import java.util.*;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.navigation.path.PathFinder;
import io.github.jspinak.brobot.navigation.path.Paths;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;

/**
 * Manages and tracks unvisited states during comprehensive state exploration testing.
 *
 * <p>This class is a critical component of the testing exploration framework, responsible for:
 *
 * <ul>
 *   <li>Identifying states that have not been visited during test execution
 *   <li>Calculating optimal paths to reach unvisited states
 *   <li>Prioritizing which unvisited state to visit next based on distance/accessibility
 * </ul>
 *
 * <h2>Testing Strategy</h2>
 *
 * <p>The testing strategy ensures complete coverage by:
 *
 * <ul>
 *   <li>Maintaining a real-time inventory of unvisited states
 *   <li>Using path-finding algorithms to determine the shortest route to each unvisited state
 *   <li>Returning the closest unvisited state to minimize transition overhead
 *   <li>Supporting both specific start states and current active states as starting points
 * </ul>
 *
 * <h2>Path Optimization</h2>
 *
 * <p>The class uses a TreeMap to maintain unvisited states sorted by their distance score, enabling
 * efficient retrieval of the closest unvisited state. This optimization is crucial for minimizing
 * test execution time while ensuring comprehensive coverage.
 *
 * <h2>Integration with State Traversal</h2>
 *
 * <p>Works closely with {@link StateTraversalService} to guide the exploration process.
 * StateTraversalService queries this class to determine which state to visit next, ensuring
 * systematic coverage of all states in the application.
 *
 * <h2>Example Usage</h2>
 *
 * <pre>{@code
 * // Get all unvisited states
 * Set<Long> unvisited = stateExplorationTracker.getUnvisitedStates();
 *
 * // Find the closest unvisited state from current position
 * Optional<Long> nextState = stateExplorationTracker.getClosestUnvisited();
 * nextState.ifPresent(stateId -> {
 *     // Navigate to this state
 *     stateTransitionsManagement.openState(stateId);
 * });
 * }</pre>
 *
 * @see StateTraversalService for the main state exploration logic
 * @see PathFinder for path calculation between states
 * @see StateMemory for tracking active states
 * @see ExplorationOrchestrator for overall test management
 * @author jspinak
 */
@Component
public class StateExplorationTracker {

    private final StateMemory stateMemory;
    private final StateService allStatesInProjectService;
    private final PathFinder pathFinder;

    /**
     * Maps distance scores to state IDs for efficient retrieval of the closest unvisited state. The
     * TreeMap automatically sorts entries by key (distance), allowing O(log n) access to the
     * closest state.
     */
    private TreeMap<Integer, Long> unvisitedDistance = new TreeMap<>();

    public StateExplorationTracker(
            StateMemory stateMemory,
            StateService allStatesInProjectService,
            PathFinder pathFinder) {
        this.stateMemory = stateMemory;
        this.allStatesInProjectService = allStatesInProjectService;
        this.pathFinder = pathFinder;
    }

    /**
     * Retrieves all states that have not been visited during the current test run.
     *
     * <p>A state is considered unvisited if its visit count (timesVisited) is zero. This method
     * queries all states in the project and filters out those that have been visited at least once.
     *
     * <p>This information is crucial for:
     *
     * <ul>
     *   <li>Determining test coverage progress
     *   <li>Identifying states that may be unreachable
     *   <li>Planning the next steps in the exploration strategy
     * </ul>
     *
     * @return a set of state IDs representing all unvisited states in the project. Returns an empty
     *     set if all states have been visited.
     * @see State#getTimesVisited() for visit tracking
     */
    public Set<Long> getUnvisitedStates() {
        Set<Long> unvisited = new HashSet<>();
        allStatesInProjectService
                .getAllStates()
                .forEach(
                        st -> {
                            if (st.getTimesVisited() == 0) unvisited.add(st.getId());
                        });
        return unvisited;
    }

    /**
     * Finds the closest unvisited state from a given set of starting states.
     *
     * <p>This method implements the core path optimization logic by:
     *
     * <ol>
     *   <li>Identifying all unvisited states in the project
     *   <li>Calculating paths from the start states to each unvisited state
     *   <li>Scoring each path based on distance/complexity
     *   <li>Returning the unvisited state with the lowest score (shortest path)
     * </ol>
     *
     * <p>The scoring mechanism considers factors such as:
     *
     * <ul>
     *   <li>Number of transitions required
     *   <li>Reliability of transitions
     *   <li>Overall path complexity
     * </ul>
     *
     * <p>States with no valid path (score <= 0) are excluded from consideration, as they may be
     * unreachable from the current position.
     *
     * @param startStates the set of state IDs to use as starting points for path calculation.
     *     Typically includes all currently active states.
     * @return an Optional containing the ID of the closest unvisited state, or empty if no
     *     reachable unvisited states exist
     * @throws NullPointerException if startStates is null
     * @see PathFinder#getPathsToState(Set, Long) for path calculation details
     * @see Paths#getBestScore() for scoring methodology
     */
    public Optional<Long> getClosestUnvisited(Set<Long> startStates) {
        Set<Long> unvisited = getUnvisitedStates();
        unvisitedDistance = new TreeMap<>();
        unvisited.forEach(
                state -> {
                    Paths paths = pathFinder.getPathsToState(startStates, state);
                    int score = paths.getBestScore();
                    if (score > 0) unvisitedDistance.put(score, state);
                });
        if (unvisitedDistance.isEmpty()) return Optional.empty();
        return Optional.ofNullable(unvisitedDistance.firstEntry().getValue());
    }

    /**
     * Finds the closest unvisited state from the current active states.
     *
     * <p>This convenience method uses the currently active states as starting points, making it
     * ideal for real-time exploration decisions during test execution. It delegates to {@link
     * #getClosestUnvisited(Set)} with the active states from StateMemory.
     *
     * <p>This is the primary method used during automated exploration, as it automatically
     * considers the current application state when determining the next state to visit.
     *
     * @return an Optional containing the ID of the closest unvisited state from the current
     *     position, or empty if no reachable unvisited states exist
     * @see StateMemory#getActiveStates() for current state information
     * @see #getClosestUnvisited(Set) for the underlying logic
     */
    public Optional<Long> getClosestUnvisited() {
        return getClosestUnvisited(stateMemory.getActiveStates());
    }
}
