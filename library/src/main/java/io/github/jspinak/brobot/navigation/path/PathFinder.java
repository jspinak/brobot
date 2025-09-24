package io.github.jspinak.brobot.navigation.path;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.navigation.transition.StateTransitionsJointTable;

import lombok.extern.slf4j.Slf4j;

/**
 * Implements graph traversal algorithms to find navigation paths between States.
 *
 * <p>PathFinder is a core component of the Path Traversal Model in the Brobot framework,
 * responsible for discovering all possible routes from a set of starting states to a target state.
 * It treats the state structure as a directed graph and uses recursive traversal to find valid
 * paths.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li><b>Multi-path Discovery</b>: Finds all valid paths, not just the shortest one
 *   <li><b>Path Scoring</b>: Evaluates paths based on state weights to prioritize optimal routes
 *   <li><b>Cycle Prevention</b>: Avoids infinite loops by tracking visited states
 *   <li><b>Multi-start Support</b>: Can begin from multiple possible starting states
 * </ul>
 *
 * <p>The pathfinding algorithm:
 *
 * <ol>
 *   <li>Starts from the target state and works backwards
 *   <li>Recursively explores all states that have transitions to the current state
 *   <li>Terminates paths when reaching any of the start states
 *   <li>Scores and sorts discovered paths for optimal selection
 * </ol>
 *
 * <p>This approach enables the framework to automatically navigate complex GUI structures, recover
 * from unexpected states, and adapt to changes in the application flow. The ability to find
 * alternative paths is crucial for robust automation that can handle variations in GUI behavior.
 *
 * @since 1.0
 * @see Path
 * @see Paths
 * @see StateTransitions
 * @see StateTransitionsJointTable
 */
@Component
@Slf4j
public class PathFinder {

    private final StateTransitionsJointTable stateTransitionsJointTable;
    private final StateService allStates;
    private final StateTransitionService stateTransitionsInProjectService;

    // Instance fields used during path finding - access is synchronized
    private Set<Long> startStates;
    private List<Path> pathList;

    public PathFinder(
            StateTransitionsJointTable stateTransitionsJointTable,
            StateService allStatesInProjectService,
            StateTransitionService stateTransitionsInProjectService) {
        this.stateTransitionsJointTable = stateTransitionsJointTable;
        this.allStates = allStatesInProjectService;
        this.stateTransitionsInProjectService = stateTransitionsInProjectService;
    }

    public Paths getPathsToState(List<State> startStates, State targetState) {
        if (targetState == null) {
            return getPathsToState(new HashSet<>(), null);
        }
        Set<Long> startStateIds = new HashSet<>();
        startStates.forEach(ss -> startStateIds.add(ss.getId()));
        return getPathsToState(startStateIds, targetState.getId());
    }

    public synchronized Paths getPathsToState(Set<Long> startStates, Long targetState) {
        // Handle null target state
        if (targetState == null) {
            return new Paths(new ArrayList<>());
        }

        // Handle empty start states
        if (startStates == null || startStates.isEmpty()) {
            String errorMsg =
                    "Cannot find path: No active start states. "
                            + "Please ensure initial states are set before attempting navigation.";
            return new Paths(new ArrayList<>());
        }

        String targetStateName = allStates.getStateName(targetState);
        String startStatesString =
                startStates.stream()
                        .map(allStates::getStateName)
                        .filter(Objects::nonNull)
                        .reduce((s, s2) -> s + ", " + s2)
                        .orElse("");

        // Use instance fields - method is synchronized for thread safety
        this.startStates = startStates;
        this.pathList = new ArrayList<>();
        recursePath(new Path(), targetState);

        if (pathList.isEmpty()) {
            log.debug("No paths found from {} to {}", startStatesString, targetStateName);
        } else {
            log.info(
                    "Found {} path(s) from [{}] to {}",
                    pathList.size(),
                    startStatesString,
                    targetStateName);
        }
        Paths paths = new Paths(pathList);
        paths.sort();
        paths.print(allStates);
        return paths;
    }

    private void recursePath(Path path, Long stateInFocus) {
        String stateName = allStates.getStateName(stateInFocus);
        log.debug("Recursing for state: {}, current path: {}", stateName, path.getStatesAsString());

        if (log.isDebugEnabled()) {
            String startStatesString =
                    startStates.stream()
                            .map(allStates::getStateName)
                            .filter(Objects::nonNull)
                            .reduce((s, s2) -> s + ", " + s2)
                            .orElse("");
            log.debug("Start states: {}", startStatesString);
        }

        if (!path.contains(stateInFocus)) {
            path.add(stateInFocus);
            addTransition(path);
            if (startStates.contains(stateInFocus)) {
                Path successfulPath = path.getCopy();
                successfulPath.reverse();
                setPathScore(successfulPath);
                pathList.add(successfulPath);
                log.debug(
                        "Found path: {}, total paths: {}",
                        path.getStatesAsString(),
                        pathList.size());
            } else {
                Set<Long> parentStates =
                        stateTransitionsJointTable.getStatesWithTransitionsTo(stateInFocus);
                if (log.isDebugEnabled() && !parentStates.isEmpty()) {
                    String parentStatesAsString =
                            parentStates.stream()
                                    .map(allStates::getStateName)
                                    .filter(Objects::nonNull)
                                    .reduce((s, s2) -> s + ", " + s2)
                                    .orElse("");
                    log.debug("Parent states for {}: {}", stateName, parentStatesAsString);
                }
                for (Long newState : parentStates) {
                    recursePath(path, newState);
                }
            }
        }
        if (Objects.equals(path.get(path.size() - 1), stateInFocus)) path.remove(stateInFocus);
        log.debug("Finished recursing for state: {}", stateName);
    }

    private void addTransition(Path path) {
        if (path.size() <= 1) return; // no transitions if only one state
        Long fromState = path.get(path.size() - 1);
        Long toState = path.get(path.size() - 2);
        Optional<StateTransition> transition =
                stateTransitionsInProjectService.getTransition(fromState, toState);
        transition.ifPresent(path::add);
    }

    private void setPathScore(Path path) {
        int score = 0;
        // Sum state costs
        for (Long stateId : path.getStates()) {
            Optional<State> state = allStates.getState(stateId);
            if (state.isPresent()) score += state.get().getPathCost();
        }
        // Sum transition costs
        for (StateTransition stateTrans : path.getTransitions()) {
            score += stateTrans.getPathCost();
        }
        path.setPathCost(score);
    }
}
