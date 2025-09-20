package io.github.jspinak.brobot.navigation.path;

import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.transition.StateNavigator;

/**
 * Manages path scoring and recovery after failed state traversals.
 *
 * <p>PathManager handles the complex task of maintaining valid navigation paths when state
 * transitions fail partway through execution. In real-world automation, transitions can fail due to
 * timeouts, unexpected popups, network issues, or application errors. When this occurs, the
 * automation may be stranded at an intermediate state along the intended path. PathManager provides
 * the intelligence to recover by recalculating viable paths from the current position.
 *
 * <p>Key responsibilities:
 *
 * <ul>
 *   <li><b>Score Calculation</b>: Compute path scores based on constituent state scores
 *   <li><b>Path Validation</b>: Remove paths invalidated by failed transitions
 *   <li><b>Path Truncation</b>: Shorten paths to start from current active states
 *   <li><b>Score Updates</b>: Recalculate scores after path modifications
 *   <li><b>Path Sorting</b>: Maintain paths in score-optimal order
 * </ul>
 *
 * <p>Failure recovery process:
 *
 * <ol>
 *   <li>Transition fails at some point along a path
 *   <li>Current active states are identified
 *   <li>Paths through the failed transition are removed
 *   <li>Remaining paths are truncated to start from active states
 *   <li>Path scores are recalculated based on new routes
 *   <li>Paths are re-sorted by score for optimal selection
 * </ol>
 *
 * <p>Path scoring mechanism:
 *
 * <ul>
 *   <li>Each state has an individual path score
 *   <li>Path score = sum of all constituent state scores
 *   <li>Lower scores indicate preferred paths
 *   <li>Scores reflect difficulty, reliability, or time cost
 *   <li>Dynamic recalculation ensures current accuracy
 * </ul>
 *
 * <p>Common failure scenarios handled:
 *
 * <ul>
 *   <li>Timeout waiting for state appearance
 *   <li>Unexpected dialog blocking transition
 *   <li>Network delay preventing page load
 *   <li>Application crash during navigation
 *   <li>User intervention disrupting automation
 * </ul>
 *
 * <p>Example recovery:
 *
 * <pre>
 * // Original path: Login -> Menu -> Settings -> Advanced
 * // Fails at: Menu -> Settings transition
 * // Active states: {Menu}
 * // Recovery: Remove paths through failed transition
 * //          Find alternative: Menu -> Help -> Settings -> Advanced
 * </pre>
 *
 * <p>Integration with Path Traversal Model (ξ):
 *
 * <ul>
 *   <li>Maintains viable paths in the traversal model
 *   <li>Ensures path scores reflect current state graph
 *   <li>Enables adaptive navigation despite failures
 *   <li>Supports the stochastic nature of GUI automation
 * </ul>
 *
 * <p>In the model-based approach, PathManager embodies the resilience needed for robust automation.
 * By maintaining and adapting navigation paths in response to failures, it ensures the automation
 * can recover and continue rather than failing completely when encountering unexpected situations.
 *
 * @since 1.0
 * @see Path
 * @see Paths
 * @see State
 * @see PathTraverser
 * @see StateNavigator
 */
@Component
public class PathManager {

    private StateService allStatesInProjectService;

    public PathManager(StateService allStatesInProjectService) {
        this.allStatesInProjectService = allStatesInProjectService;
    }

    public void updateScore(Path path) {
        int score = 0;
        for (Long stateId : path.getStates()) {
            Optional<State> optState = allStatesInProjectService.getState(stateId);
            if (optState.isPresent()) score += optState.get().getPathCost();
        }
        path.setPathCost(score);
    }

    public void updateScores(Paths paths) {
        paths.getPaths().forEach(this::updateScore);
        paths.sort();
    }

    public Paths getCleanPaths(Set<Long> activeStates, Paths paths, Long failedTransitionStart) {
        Paths cleanPaths = paths.cleanPaths(activeStates, failedTransitionStart);
        updateScores(cleanPaths);
        return cleanPaths;
    }
}
