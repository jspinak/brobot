package io.github.jspinak.brobot.navigation.transition;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.config.automation.AutomationConfig;
import io.github.jspinak.brobot.exception.AutomationException;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.navigation.path.PathFinder;
import io.github.jspinak.brobot.navigation.path.PathManager;
import io.github.jspinak.brobot.navigation.path.PathTraverser;
import io.github.jspinak.brobot.navigation.path.Paths;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;

import lombok.extern.slf4j.Slf4j;

/**
 * High-level state navigation orchestrator for the Brobot framework.
 *
 * <p>StateNavigator provides the primary API for navigating to target states in the GUI. It
 * orchestrates the complete navigation process by finding paths, attempting transitions, handling
 * failures, and recovering through alternative routes. This class embodies the framework's
 * intelligent navigation capabilities that distinguish model-based from script-based automation.
 *
 * <p>Navigation strategy:
 *
 * <ol>
 *   <li><b>Path Discovery</b>: Find all possible routes from current position to target
 *   <li><b>Path Selection</b>: Choose the path with lowest score (most reliable/efficient)
 *   <li><b>Path Traversal</b>: Execute transitions along the selected path
 *   <li><b>Failure Recovery</b>: On failure, remove failed path and try alternatives
 *   <li><b>Path Adaptation</b>: Adjust remaining paths based on new position after partial success
 * </ol>
 *
 * <p>Key features:
 *
 * <ul>
 *   <li><b>Automatic Pathfinding</b>: Discovers routes without explicit programming
 *   <li><b>Failure Resilience</b>: Automatically tries alternative paths on failure
 *   <li><b>Partial Progress</b>: Leverages partial path success to reduce remaining work
 *   <li><b>Transition Blacklisting</b>: Failed transitions are avoided in subsequent attempts
 *   <li><b>Session Logging</b>: Complete audit trail of navigation attempts and outcomes
 * </ul>
 *
 * <p>Failure handling philosophy:
 *
 * <ul>
 *   <li>Failed transitions are assumed to be persistently broken
 *   <li>Retry logic belongs in individual transitions, not the orchestrator
 *   <li>Each transition can implement its own retry strategy
 *   <li>This provides granular control over failure handling
 * </ul>
 *
 * <p>Common scenarios:
 *
 * <ul>
 *   <li><b>Direct Navigation</b>: Target is directly reachable from current state
 *   <li><b>Multi-hop Navigation</b>: Must traverse intermediate states to reach target
 *   <li><b>Recovery Navigation</b>: Primary path fails, alternative route succeeds
 *   <li><b>Already There</b>: Target state is already active, just needs finalization
 * </ul>
 *
 * <p>Integration with logging:
 *
 * <ul>
 *   <li>Records transition attempts with timing information
 *   <li>Logs state changes for debugging and analysis
 *   <li>Tracks success/failure rates for optimization
 *   <li>Provides session-based audit trails
 * </ul>
 *
 * <p>In the model-based approach, StateNavigator represents the intelligence layer that transforms
 * static state graphs into dynamic navigation capabilities. It enables automation that can adapt to
 * GUI variations, recover from failures, and find alternative routes - capabilities that would
 * require extensive manual coding in traditional approaches.
 *
 * @since 1.0
 * @see PathFinder
 * @see PathTraverser
 * @see PathManager
 * @see StateMemory
 * @see ActionLogger
 */
@Slf4j
@Component
public class StateNavigator {

    private final PathFinder pathFinder;
    private final StateService allStatesInProjectService;
    private final StateMemory stateMemory;
    private final PathTraverser pathTraverser;
    private final PathManager pathManager;

    @Autowired(required = false)
    private AutomationConfig automationConfig;

    Set<Long> activeStates;

    public StateNavigator(
            PathFinder pathFinder,
            StateService allStatesInProjectService,
            StateMemory stateMemory,
            PathTraverser pathTraverser,
            PathManager pathManager) {
        this.pathFinder = pathFinder;
        this.allStatesInProjectService = allStatesInProjectService;
        this.stateMemory = stateMemory;
        this.pathTraverser = pathTraverser;
        this.pathManager = pathManager;
    }

    /**
     * Navigates to a target state by name.
     *
     * <p>Convenience method that converts state name to ID and delegates to the ID-based navigation
     * method.
     *
     * @param stateName Name of the target state to navigate to
     * @return true if navigation succeeded, false otherwise
     * @see #openState(Long)
     */
    public boolean openState(String stateName) {
        try {
            Long stateToOpen = allStatesInProjectService.getStateId(stateName);
            if (stateToOpen == null) {
                String errorMsg = "Target state not found: " + stateName;
                handleNavigationFailure(errorMsg, stateName, "state_lookup", false);
                return false;
            }
            return openState(stateToOpen);
        } catch (Exception e) {
            String errorMsg = "Failed to navigate to state: " + stateName;
            log.error(errorMsg, e);
            handleNavigationFailure(errorMsg, stateName, "navigation", false);
            return false;
        }
    }

    /**
     * Opens the specified state using its enum identifier.
     *
     * <p>This convenience method simplifies navigation by allowing direct use of StateEnum values,
     * eliminating the need for manual toString() calls and providing type safety at compile time.
     *
     * @param stateEnum Enum representing the target state
     * @return true if navigation succeeded, false otherwise
     * @see #openState(String)
     */
    public boolean openState(io.github.jspinak.brobot.model.state.StateEnum stateEnum) {
        return openState(stateEnum.toString());
    }

    /**
     * Navigates to a target state by ID.
     *
     * <p>Orchestrates the complete navigation process:
     *
     * <ol>
     *   <li>Records current active states
     *   <li>Finds all possible paths to target
     *   <li>Attempts paths in order of preference
     *   <li>Handles failures and tries alternatives
     *   <li>Logs complete transition details
     * </ol>
     *
     * <p>Side effects:
     *
     * <ul>
     *   <li>Updates StateMemory with new active states
     *   <li>Logs transition attempts to ActionLogger
     *   <li>May partially navigate even on overall failure
     * </ul>
     *
     * @param stateToOpen ID of the target state to navigate to
     * @return true if target state was successfully reached, false otherwise
     */
    public boolean openState(Long stateToOpen) {
        Instant startTime = Instant.now();
        activeStates = stateMemory.getActiveStates();

        // Check if there are any active states for pathfinding
        if (activeStates.isEmpty()) {
            String errorMsg =
                    "No initial states are active. Brobot cannot perform pathfinding without active"
                            + " states.\n"
                            + "Please set initial states using one of these methods:\n"
                            + "  1. Use @State(initial = true) annotation on your state class\n"
                            + "  2. Call InitialStates.addStateSet() to register initial states\n"
                            + "  3. Use InitialStateVerifier.verify() to find and activate initial"
                            + " states";
            log.error(errorMsg);
            return false;
        }

        Optional<State> targetState = allStatesInProjectService.getState(stateToOpen);
        if (targetState.isEmpty()) {
            return false;
        }
        // Log transition attempt start
        String transitionDescription =
                "Transition from "
                        + stateMemory.getActiveStateNamesAsString()
                        + " to "
                        + targetState.get().getName();
        //         automationSession.getCurrentSessionId(),
        //         "Transition start:",
        //         transitionDescription,
        //         "info");

        // we find the paths once, and then reuse these paths when needed
        Paths paths = pathFinder.getPathsToState(activeStates, stateToOpen);
        String sessionId =
                UUID.randomUUID().toString(); // Generate session ID for this navigation attempt
        boolean success = recursePaths(paths, stateToOpen, sessionId);
        Duration duration = Duration.between(startTime, Instant.now());

        // Log transition attempt end
        Set<State> activeStatesSet = allStatesInProjectService.findSetById(activeStates);
        //         sessionId, // no duration for start log
        //         activeStatesSet,
        //         Collections.singleton(targetState.get()),
        //         allStatesInProjectService.findSetById(activeStates),
        //         success,
        //         duration.toMillis());

        if (!success) {
            String targetStateName = targetState.get().getName();
            handleNavigationFailure(
                    "Failed to navigate to state after trying all paths: " + targetStateName,
                    targetStateName,
                    "path_traversal",
                    true // potentially recoverable with retry
                    );
        }
        String activeStatesStr =
                activeStates.stream()
                        .map(id -> id + "(" + allStatesInProjectService.getStateName(id) + ")")
                        .reduce("", (s1, s2) -> s1.isEmpty() ? s2 : s1 + ", " + s2);
        return success;
    }

    /**
     * Recursively attempts paths until target is reached or all paths exhausted.
     *
     * <p>Core navigation algorithm that implements intelligent path selection and failure recovery:
     *
     * <ol>
     *   <li>Base case: No paths available - navigation fails
     *   <li>Optimization: If already at target, just finalize transition
     *   <li>Attempt: Try the best-scoring path
     *   <li>Success: Path traversal completed - navigation succeeds
     *   <li>Failure: Clean paths based on failure point and recurse
     * </ol>
     *
     * <p>Failure handling strategy:
     *
     * <ul>
     *   <li>Failed transitions are blacklisted for this navigation attempt
     *   <li>Retry logic belongs in individual transitions, not here
     *   <li>Paths containing failed transitions are removed
     *   <li>Remaining paths are adjusted for new position
     * </ul>
     *
     * <p>This recursive approach ensures all viable paths are attempted while avoiding infinite
     * loops through failed transition tracking.
     *
     * @param paths Available paths to attempt (sorted by score)
     * @param stateToOpen Target state ID
     * @param sessionId Current automation session for logging
     * @return true if target reached, false if all paths exhausted
     */
    private boolean recursePaths(Paths paths, Long stateToOpen, String sessionId) {
        if (paths.isEmpty()) return false;
        if (activeStates.contains(stateToOpen) && pathTraverser.finishTransition(stateToOpen))
            return true;
        if (pathTraverser.traverse(paths.getPaths().getFirst()))
            return true; // false if a transition fails
        activeStates = stateMemory.getActiveStates(); // may have changed after traversal
        return recursePaths(
                pathManager.getCleanPaths(
                        activeStates, paths, pathTraverser.getFailedTransitionStartState()),
                stateToOpen,
                sessionId);
    }

    /**
     * Handles navigation failures based on configured behavior.
     *
     * <p>This method determines how to handle automation failures based on the AutomationConfig:
     *
     * <ul>
     *   <li>Log the failure with appropriate detail level
     *   <li>Throw an exception if configured to do so
     *   <li>Exit the application if configured to do so
     * </ul>
     *
     * @param errorMessage Error message describing the failure
     * @param stateName Name of the state involved in the failure
     * @param operation Operation that failed (e.g., "navigation", "transition")
     * @param recoverable Whether the error is potentially recoverable
     */
    private void handleNavigationFailure(
            String errorMessage, String stateName, String operation, boolean recoverable) {
        // If no config is provided, use default behavior (just log)
        if (automationConfig == null) {
            log.error("Navigation failure: {}", errorMessage);
            return;
        }

        // Log the failure
        if (automationConfig.isLogStackTraces()) {
            log.error(
                    "Navigation failure in state '{}' during '{}': {}",
                    stateName,
                    operation,
                    errorMessage);
        } else {
            log.error("Navigation failure: {}", errorMessage);
        }

        // Throw exception if configured
        if (automationConfig.isThrowOnFailure()) {
            throw new AutomationException(errorMessage, stateName, operation, recoverable);
        }

        // Exit application if configured
        if (automationConfig.isExitOnFailure()) {
            log.error(
                    "Exiting application due to navigation failure"
                            + " (brobot.automation.exitOnFailure=true)");
            System.exit(automationConfig.getFailureExitCode());
        }
    }
}
