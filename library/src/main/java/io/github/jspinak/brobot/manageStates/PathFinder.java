package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.reports.Output;
import io.github.jspinak.brobot.reports.Report;
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
    private final AllStatesInProjectService allStatesInProjectService;
    private final StateTransitionsInProjectService stateTransitionsInProjectService;

    private Set<Long> startStates;
    private List<Path> pathList;

    public PathFinder(StateTransitionsJointTable stateTransitionsJointTable, AllStatesInProjectService allStatesInProjectService,
                      StateTransitionsInProjectService stateTransitionsInProjectService) {
        this.stateTransitionsJointTable = stateTransitionsJointTable;
        this.allStatesInProjectService = allStatesInProjectService;
        this.stateTransitionsInProjectService = stateTransitionsInProjectService;
    }

    public Paths getPathsToState(List<State> startStates, State targetState) {
        Set<Long> startStateIds = new HashSet<>();
        startStates.forEach(ss -> startStateIds.add(ss.getId()));
        return getPathsToState(startStateIds, targetState.getId());
    }

    public Paths getPathsToState(Set<Long> startStates, Long targetState) {
        Report.println("Find path: " + startStates + " -> " + targetState);
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
        System.out.println("Recursing for state: " + stateInFocus);
        System.out.println("Current path: " + path.getStates());
        System.out.println("Start states: " + startStates);

        if (!path.contains(stateInFocus)) {
            path.add(stateInFocus);
            addTransition(path);
            if (startStates.contains(stateInFocus)) {
                System.out.println("Found a path: " + path.getStates());
                Path successfulPath = path.getCopy();
                successfulPath.reverse();
                setPathScore(successfulPath);
                pathList.add(successfulPath);
                System.out.println("Added path to pathList. pathList size: " + pathList.size());
            } else {
                Set<Long> parentStates = stateTransitionsJointTable.getStatesWithTransitionsTo(stateInFocus);
                System.out.println("Parent states for " + stateInFocus + ": " + parentStates);
                for (Long newState : parentStates) {
                    recursePath(path, newState);
                }
            }
        }
        if (Objects.equals(path.get(path.size() - 1), stateInFocus)) path.remove(stateInFocus);
        System.out.println("Finished recursing for state: " + stateInFocus);
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
            Optional<State> state = allStatesInProjectService.getState(stateId);
            if (state.isPresent()) score += state.get().getPathScore();
        }
        for (IStateTransition stateTrans : path.getTransitions()) {
            score += stateTrans.getScore();
        }
        path.setScore(score);
    }


}
