package io.github.jspinak.brobot.navigation.path;

import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.navigation.transition.StateTransitionsJointTable;
import io.github.jspinak.brobot.tools.logging.MessageFormatter;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.model.state.State;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Implements graph traversal algorithms to find navigation paths between States.
 * 
 * <p>PathFinder is a core component of the Path Traversal Model (ξ) in the Brobot framework,
 * responsible for discovering all possible routes from a set of starting states to a target 
 * state. It treats the state structure as a directed graph and uses recursive traversal to 
 * find valid paths.</p>
 * 
 * <p>Key features:
 * <ul>
 *   <li><b>Multi-path Discovery</b>: Finds all valid paths, not just the shortest one</li>
 *   <li><b>Path Scoring</b>: Evaluates paths based on state weights to prioritize optimal routes</li>
 *   <li><b>Cycle Prevention</b>: Avoids infinite loops by tracking visited states</li>
 *   <li><b>Multi-start Support</b>: Can begin from multiple possible starting states</li>
 * </ul>
 * </p>
 * 
 * <p>The pathfinding algorithm:
 * <ol>
 *   <li>Starts from the target state and works backwards</li>
 *   <li>Recursively explores all states that have transitions to the current state</li>
 *   <li>Terminates paths when reaching any of the start states</li>
 *   <li>Scores and sorts discovered paths for optimal selection</li>
 * </ol>
 * </p>
 * 
 * <p>This approach enables the framework to automatically navigate complex GUI structures,
 * recover from unexpected states, and adapt to changes in the application flow. The ability
 * to find alternative paths is crucial for robust automation that can handle variations
 * in GUI behavior.</p>
 * 
 * @since 1.0
 * @see Path
 * @see Paths
 * @see StateTransitions
 * @see StateTransitionsJointTable
 */
@Component
public class PathFinder {

    private final StateTransitionsJointTable stateTransitionsJointTable;
    private final StateService allStates;
    private final StateTransitionService stateTransitionsInProjectService;

    // Instance fields used during path finding - access is synchronized
    private Set<Long> startStates;
    private List<Path> pathList;

    public PathFinder(StateTransitionsJointTable stateTransitionsJointTable, StateService allStatesInProjectService,
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
            ConsoleReporter.println(MessageFormatter.fail + "Target state is null");
            return new Paths(new ArrayList<>());
        }
        
        String targetStateName = allStates.getStateName(targetState);
        String startStatesString = startStates.stream()
                .map(allStates::getStateName)
                .reduce("", (s, s2) -> s + ", " + s2);
        ConsoleReporter.println("Find path: " + startStatesString + " -> " + targetStateName);
        
        // Use instance fields - method is synchronized for thread safety
        this.startStates = startStates;
        this.pathList = new ArrayList<>();
        recursePath(new Path(), targetState);
        
        if (pathList.isEmpty()) ConsoleReporter.println(MessageFormatter.fail + "Path to state not found.");
        Paths paths = new Paths(pathList);
        paths.sort();
        paths.print(allStates);
        return paths;
    }

    private void recursePath(Path path, Long stateInFocus) {
        System.out.println("Recursing for state: " + allStates.getStateName(stateInFocus));
        System.out.println("Current path: " + path.getStatesAsString());
        String startStatesString = startStates.stream()
                .map(allStates::getStateName)
                .reduce("", (s, s2) -> s + ", " + s2);
        System.out.println("Start states: " + startStatesString);

        if (!path.contains(stateInFocus)) {
            path.add(stateInFocus);
            addTransition(path);
            if (startStates.contains(stateInFocus)) {
                System.out.println("Found a path: " + path.getStatesAsString());
                Path successfulPath = path.getCopy();
                successfulPath.reverse();
                setPathScore(successfulPath);
                pathList.add(successfulPath);
                System.out.println("Added path to pathList. pathList size: " + pathList.size());
            } else {
                Set<Long> parentStates = stateTransitionsJointTable.getStatesWithTransitionsTo(stateInFocus);
                String parentStatesAsString = parentStates.stream()
                        .map(allStates::getStateName)
                        .reduce("", (s, s2) -> s + ", " + s2);
                System.out.println("Parent states for " + allStates.getStateName(stateInFocus) + ": " + parentStatesAsString);
                for (Long newState : parentStates) {
                    recursePath(path, newState);
                }
            }
        }
        if (Objects.equals(path.get(path.size() - 1), stateInFocus)) path.remove(stateInFocus);
        System.out.println("Finished recursing for state: " + allStates.getStateName(stateInFocus));
    }

    private void addTransition(Path path) {
        if (path.size() <= 1) return; // no transitions if only one state
        Long fromState = path.get(path.size() - 1);
        Long toState = path.get(path.size() - 2);
        Optional<StateTransition> transition = stateTransitionsInProjectService.getTransition(fromState, toState);
        transition.ifPresent(path::add);
    }

    private void setPathScore(Path path) {
        int score = 0;
        for (Long stateId : path.getStates()) {
            Optional<State> state = allStates.getState(stateId);
            if (state.isPresent()) score += state.get().getPathScore();
        }
        for (StateTransition stateTrans : path.getTransitions()) {
            score += stateTrans.getScore();
        }
        path.setScore(score);
    }


}
