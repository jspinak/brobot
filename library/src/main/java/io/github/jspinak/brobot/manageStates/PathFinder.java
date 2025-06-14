package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.report.Output;
import io.github.jspinak.brobot.report.Report;
import io.github.jspinak.brobot.services.StateTransitionsInProjectService;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Finds all paths from a set of start States to a target State.
 * The Path list is sorted in ascending order for Path score.
 */
@Component
public class PathFinder {

    private final StateTransitionsJointTable stateTransitionsJointTable;
    private final AllStatesInProjectService allStates;
    private final StateTransitionsInProjectService stateTransitionsInProjectService;

    private Set<Long> startStates;
    private List<Path> pathList;

    public PathFinder(StateTransitionsJointTable stateTransitionsJointTable, AllStatesInProjectService allStatesInProjectService,
                      StateTransitionsInProjectService stateTransitionsInProjectService) {
        this.stateTransitionsJointTable = stateTransitionsJointTable;
        this.allStates = allStatesInProjectService;
        this.stateTransitionsInProjectService = stateTransitionsInProjectService;
    }

    public Paths getPathsToState(List<State> startStates, State targetState) {
        Set<Long> startStateIds = new HashSet<>();
        startStates.forEach(ss -> startStateIds.add(ss.getId()));
        return getPathsToState(startStateIds, targetState.getId());
    }

    public Paths getPathsToState(Set<Long> startStates, Long targetState) {
        String targetStateName = allStates.getStateName(targetState);
        String startStatesString = startStates.stream()
                .map(allStates::getStateName)
                .reduce("", (s, s2) -> s + ", " + s2);
        Report.println("Find path: " + startStatesString + " -> " + targetStateName);
        this.startStates = startStates;
        pathList = new ArrayList<>();
        recursePath(new Path(), targetState);
        if (pathList.isEmpty()) Report.println(Output.fail + "Path to state not found.");
        Paths paths = new Paths(pathList);
        paths.sort();
        paths.print();
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
        Optional<IStateTransition> transition = stateTransitionsInProjectService.getTransition(fromState, toState);
        transition.ifPresent(path::add);
    }

    private void setPathScore(Path path) {
        int score = 0;
        for (Long stateId : path.getStates()) {
            Optional<State> state = allStates.getState(stateId);
            if (state.isPresent()) score += state.get().getPathScore();
        }
        for (IStateTransition stateTrans : path.getTransitions()) {
            score += stateTrans.getScore();
        }
        path.setScore(score);
    }


}
