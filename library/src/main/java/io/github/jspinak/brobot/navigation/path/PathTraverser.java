package io.github.jspinak.brobot.navigation.path;

import java.util.Optional;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.state.special.SpecialStateType;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.navigation.transition.TransitionConditionPackager;
import io.github.jspinak.brobot.navigation.transition.TransitionExecutor;

import lombok.Getter;

/**
 * Executes navigation paths by performing state transitions in sequence.
 *
 * <p>PathTraverser is the execution component of the Path Traversal Model (§) in the Brobot
 * framework, responsible for converting abstract Path objects into concrete navigation actions. It
 * orchestrates the sequential execution of state transitions, monitors success and failure, and
 * tracks failure points for recovery operations.
 *
 * <p>Key responsibilities:
 *
 * <ul>
 *   <li><b>Path Execution</b>: Traverses states in order by invoking transitions
 *   <li><b>Failure Detection</b>: Identifies when and where transitions fail
 *   <li><b>Failure Tracking</b>: Records the starting state of failed transitions
 *   <li><b>Transition Completion</b>: Ensures final state transitions complete properly
 * </ul>
 *
 * <p>Traversal process:
 *
 * <ol>
 *   <li>Iterates through consecutive state pairs in the path
 *   <li>Executes the transition from current state to next state
 *   <li>Monitors transition success/failure
 *   <li>On failure, records the failure point and halts
 *   <li>On success, continues to next transition
 * </ol>
 *
 * <p>Failure handling:
 *
 * <ul>
 *   <li>When a transition fails, the starting state is recorded
 *   <li>This information enables PathManager to clean invalid paths
 *   <li>Allows recovery by finding alternative paths from current position
 *   <li>Supports the framework's resilience to GUI variations
 * </ul>
 *
 * <p>Integration with model-based approach:
 *
 * <ul>
 *   <li>Transforms abstract paths into concrete actions
 *   <li>Provides feedback for path adaptation and learning
 *   <li>Enables dynamic recovery from navigation failures
 *   <li>Supports the stochastic nature of GUI environments
 * </ul>
 *
 * <p>In the model-based paradigm, PathTraverser bridges the gap between planning (pathfinding) and
 * execution (state transitions). It embodies the framework's ability to not just plan navigation
 * routes but actually execute them while adapting to real-world conditions.
 *
 * @since 1.0
 * @see Path
 * @see Paths
 * @see PathManager
 * @see TransitionExecutor
 * @see StateTransitions
 */
@Component
@Getter
public class PathTraverser {

    private final TransitionExecutor doTransition;
    private final StateTransitionService stateTransitionsInProjectService;
    private final TransitionConditionPackager transitionBooleanSupplierPackager;

    /**
     * The ID of the state from which the last transition failure originated. Used by PathManager to
     * filter out paths containing failed transitions. Defaults to NULL state ID when no failures
     * have occurred.
     */
    private Long failedTransitionStartState = SpecialStateType.NULL.getId();

    public PathTraverser(
            TransitionExecutor doTransition,
            StateTransitionService stateTransitionsInProjectService,
            TransitionConditionPackager transitionBooleanSupplierPackager) {
        this.doTransition = doTransition;
        this.stateTransitionsInProjectService = stateTransitionsInProjectService;
        this.transitionBooleanSupplierPackager = transitionBooleanSupplierPackager;
    }

    /**
     * Executes all transitions in a path sequentially.
     *
     * <p>Iterates through consecutive state pairs in the path, executing the transition between
     * each pair. If any transition fails, the traversal stops immediately and records the failure
     * point for recovery operations.
     *
     * @param path The navigation path containing states and transitions to execute
     * @return true if all transitions complete successfully, false if any transition fails
     */
    public boolean traverse(Path path) {
        for (int i = 0; i < path.size() - 1; i++) {
            if (!doTransition.go(path.get(i), path.get(i + 1))) {
                failedTransitionStartState = path.get(i);
                return false;
            }
        }
        return true;
    }

    /**
     * Completes the final transition to a target state.
     *
     * <p>Executes the finish transition associated with the target state. This ensures that any
     * final actions required to fully enter the state are performed, such as waiting for elements
     * to load or verifying state activation.
     *
     * @param stateToOpen The ID of the state to complete transition into
     * @return true if the finish transition succeeds, false if the state has no transitions defined
     *     or if the finish transition fails
     */
    public boolean finishTransition(Long stateToOpen) {
        Optional<StateTransitions> optToStateTransitions =
                stateTransitionsInProjectService.getTransitions(stateToOpen);
        if (optToStateTransitions.isEmpty()) {
            return false;
        }
        return transitionBooleanSupplierPackager.getAsBoolean(
                optToStateTransitions.get().getTransitionFinish());
    }
}
