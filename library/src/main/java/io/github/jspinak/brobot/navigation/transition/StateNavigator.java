package io.github.jspinak.brobot.navigation.transition;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.navigation.path.PathFinder;
import io.github.jspinak.brobot.navigation.path.PathManager;
import io.github.jspinak.brobot.navigation.path.Paths;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.tools.logging.ActionLogger;
import io.github.jspinak.brobot.tools.logging.ExecutionSession;
import io.github.jspinak.brobot.tools.logging.MessageFormatter;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.navigation.path.PathTraverser;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * High-level state navigation orchestrator for the Brobot framework.
 * 
 * <p>StateNavigator provides the primary API for navigating to target states in 
 * the GUI. It orchestrates the complete navigation process by finding paths, attempting 
 * transitions, handling failures, and recovering through alternative routes. This class 
 * embodies the framework's intelligent navigation capabilities that distinguish model-based 
 * from script-based automation.</p>
 * 
 * <p>Navigation strategy:
 * <ol>
 *   <li><b>Path Discovery</b>: Find all possible routes from current position to target</li>
 *   <li><b>Path Selection</b>: Choose the path with lowest score (most reliable/efficient)</li>
 *   <li><b>Path Traversal</b>: Execute transitions along the selected path</li>
 *   <li><b>Failure Recovery</b>: On failure, remove failed path and try alternatives</li>
 *   <li><b>Path Adaptation</b>: Adjust remaining paths based on new position after partial success</li>
 * </ol>
 * </p>
 * 
 * <p>Key features:
 * <ul>
 *   <li><b>Automatic Pathfinding</b>: Discovers routes without explicit programming</li>
 *   <li><b>Failure Resilience</b>: Automatically tries alternative paths on failure</li>
 *   <li><b>Partial Progress</b>: Leverages partial path success to reduce remaining work</li>
 *   <li><b>Transition Blacklisting</b>: Failed transitions are avoided in subsequent attempts</li>
 *   <li><b>Session Logging</b>: Complete audit trail of navigation attempts and outcomes</li>
 * </ul>
 * </p>
 * 
 * <p>Failure handling philosophy:
 * <ul>
 *   <li>Failed transitions are assumed to be persistently broken</li>
 *   <li>Retry logic belongs in individual transitions, not the orchestrator</li>
 *   <li>Each transition can implement its own retry strategy</li>
 *   <li>This provides granular control over failure handling</li>
 * </ul>
 * </p>
 * 
 * <p>Common scenarios:
 * <ul>
 *   <li><b>Direct Navigation</b>: Target is directly reachable from current state</li>
 *   <li><b>Multi-hop Navigation</b>: Must traverse intermediate states to reach target</li>
 *   <li><b>Recovery Navigation</b>: Primary path fails, alternative route succeeds</li>
 *   <li><b>Already There</b>: Target state is already active, just needs finalization</li>
 * </ul>
 * </p>
 * 
 * <p>Integration with logging:
 * <ul>
 *   <li>Records transition attempts with timing information</li>
 *   <li>Logs state changes for debugging and analysis</li>
 *   <li>Tracks success/failure rates for optimization</li>
 *   <li>Provides session-based audit trails</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, StateNavigator represents the intelligence 
 * layer that transforms static state graphs into dynamic navigation capabilities. It enables 
 * automation that can adapt to GUI variations, recover from failures, and find alternative 
 * routes - capabilities that would require extensive manual coding in traditional approaches.</p>
 * 
 * @since 1.0
 * @see PathFinder
 * @see PathTraverser
 * @see PathManager
 * @see StateMemory
 * @see ActionLogger
 */
@Component
public class StateNavigator {

    private final PathFinder pathFinder;
    private final StateService allStatesInProjectService;
    private final StateMemory stateMemory;
    private final PathTraverser pathTraverser;
    private final PathManager pathManager;
    private final ActionLogger actionLogger;
    private final ExecutionSession automationSession;

    Set<Long> activeStates;

    public StateNavigator(PathFinder pathFinder, StateService allStatesInProjectService,
                                      StateMemory stateMemory, PathTraverser pathTraverser, PathManager pathManager,
                                      ActionLogger actionLogger, ExecutionSession automationSession) {
        this.pathFinder = pathFinder;
        this.allStatesInProjectService = allStatesInProjectService;
        this.stateMemory = stateMemory;
        this.pathTraverser = pathTraverser;
        this.pathManager = pathManager;
        this.actionLogger = actionLogger;
        this.automationSession = automationSession;
    }

    /**
     * Navigates to a target state by name.
     * <p>
     * Convenience method that converts state name to ID and delegates to
     * the ID-based navigation method.
     *
     * @param stateName Name of the target state to navigate to
     * @return true if navigation succeeded, false otherwise
     * @see #openState(Long)
     */
    public boolean openState(String stateName) {
        ConsoleReporter.format("Open State %s\n", stateName);
        Long stateToOpen = allStatesInProjectService.getStateId(stateName);
        if (stateToOpen == null) {
            ConsoleReporter.println(MessageFormatter.fail+" Target state not found.");
            return false;
        }
        return openState(stateToOpen);
    }

    /**
     * Navigates to a target state by ID.
     * <p>
     * Orchestrates the complete navigation process:
     * <ol>
     *   <li>Records current active states</li>
     *   <li>Finds all possible paths to target</li>
     *   <li>Attempts paths in order of preference</li>
     *   <li>Handles failures and tries alternatives</li>
     *   <li>Logs complete transition details</li>
     * </ol>
     * <p>
     * Side effects:
     * <ul>
     *   <li>Updates StateMemory with new active states</li>
     *   <li>Logs transition attempts to ActionLogger</li>
     *   <li>May partially navigate even on overall failure</li>
     * </ul>
     *
     * @param stateToOpen ID of the target state to navigate to
     * @return true if target state was successfully reached, false otherwise
     */
    public boolean openState(Long stateToOpen) {
        ConsoleReporter.format("Open State %s\n", allStatesInProjectService.getStateName(stateToOpen));
        String sessionId = automationSession.getCurrentSessionId();
        Instant startTime = Instant.now();
        activeStates = stateMemory.getActiveStates();

        Optional<State> targetState = allStatesInProjectService.getState(stateToOpen);
        if (targetState.isEmpty()) {
            ConsoleReporter.println(MessageFormatter.fail+" Target state not found.");
            return false;
        }
        // Log transition attempt start
        String transitionDescription = "Transition from " + stateMemory.getActiveStateNamesAsString() +
                " to " + targetState.get().getName();
        actionLogger.logObservation(automationSession.getCurrentSessionId(), "Transition start:", transitionDescription, "info");

        // we find the paths once, and then reuse these paths when needed
        Paths paths = pathFinder.getPathsToState(activeStates, stateToOpen);
        boolean success = recursePaths(paths, stateToOpen, sessionId);
        Duration duration = Duration.between(startTime, Instant.now());

        // Log transition attempt end
        Set<State> activeStatesSet = allStatesInProjectService.findSetById(activeStates);
        actionLogger.logStateTransition(
                sessionId, // no duration for start log
                activeStatesSet,
                Collections.singleton(targetState.get()),
                allStatesInProjectService.findSetById(activeStates),
                success,
                duration.toMillis()
        );

        if (!success) ConsoleReporter.println(MessageFormatter.fail+" All paths tried, open failed.");
        ConsoleReporter.println("Active States: " + activeStates +"\n");
        return success;
    }

    /**
     * Recursively attempts paths until target is reached or all paths exhausted.
     * <p>
     * Core navigation algorithm that implements intelligent path selection and
     * failure recovery:
     * <ol>
     *   <li>Base case: No paths available - navigation fails</li>
     *   <li>Optimization: If already at target, just finalize transition</li>
     *   <li>Attempt: Try the best-scoring path</li>
     *   <li>Success: Path traversal completed - navigation succeeds</li>
     *   <li>Failure: Clean paths based on failure point and recurse</li>
     * </ol>
     * <p>
     * Failure handling strategy:
     * <ul>
     *   <li>Failed transitions are blacklisted for this navigation attempt</li>
     *   <li>Retry logic belongs in individual transitions, not here</li>
     *   <li>Paths containing failed transitions are removed</li>
     *   <li>Remaining paths are adjusted for new position</li>
     * </ul>
     * <p>
     * This recursive approach ensures all viable paths are attempted while
     * avoiding infinite loops through failed transition tracking.
     *
     * @param paths Available paths to attempt (sorted by score)
     * @param stateToOpen Target state ID
     * @param sessionId Current automation session for logging
     * @return true if target reached, false if all paths exhausted
     */
    private boolean recursePaths(Paths paths, Long stateToOpen, String sessionId) {
        if (paths.isEmpty()) return false;
        if (activeStates.contains(stateToOpen) && pathTraverser.finishTransition(stateToOpen)) return true;
        if (pathTraverser.traverse(paths.getPaths().getFirst())) return true; // false if a transition fails
        activeStates = stateMemory.getActiveStates(); // may have changed after traversal
        return recursePaths(
                pathManager.getCleanPaths(activeStates, paths, pathTraverser.getFailedTransitionStartState()),
                stateToOpen,
                sessionId);
    }

}
