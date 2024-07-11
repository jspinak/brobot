package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.reports.Output;
import io.github.jspinak.brobot.reports.Report;
import io.github.jspinak.brobot.services.StateTransitionsService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Finds all paths from a set of start States to a target State.
 * The Path list is sorted in ascending order for Path score.
 */
@Component
public class PathFinder {

    private final StateTransitionsJointTable stateTransitionsJointTable;
    private final AllStatesInProjectService allStatesInProjectService;
    private final StateTransitionsService stateTransitionsService;

    private Set<String> startStates;
    private List<Path> pathList;

    public PathFinder(StateTransitionsJointTable stateTransitionsJointTable, AllStatesInProjectService allStatesInProjectService,
                      StateTransitionsService stateTransitionsService) {
        this.stateTransitionsJointTable = stateTransitionsJointTable;
        this.allStatesInProjectService = allStatesInProjectService;
        this.stateTransitionsService = stateTransitionsService;
    }

    public Paths getPathsToState(Set<String> startStates, String targetState) {
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

    private void recursePath(Path path, String stateInFocus) {
        if (!path.contains(stateInFocus)) {
            path.add(stateInFocus);
            addTransition(path);
            if (startStates.contains(stateInFocus)) { // a path is found
                Path successfulPath = path.getCopy();
                successfulPath.reverse();
                setPathScore(successfulPath);
                pathList.add(successfulPath);
            } else { // continue searching
                Set<String> parentStates = stateTransitionsJointTable.getStatesWithTransitionsTo(stateInFocus);
                for (String newState : parentStates) {
                    recursePath(path, newState);
                }
            }
        }
        if (path.get(path.size() - 1) == stateInFocus) path.remove(stateInFocus); // otherwise it's circular
    }

    private void addTransition(Path path) {
        if (path.size() <= 1) return; // no transitions if only one state
        String fromState = path.get(path.size() - 1);
        String toState = path.get(path.size() - 2);
        Optional<StateTransition> transition = stateTransitionsService.getTransition(fromState, toState);
        transition.ifPresent(path::add);
    }

    private void setPathScore(Path path) {
        int score = 0;
        for (String stateName : path.getStates()) {
            Optional<State> state = allStatesInProjectService.getState(stateName);
            if (state.isPresent()) score += state.get().getPathScore();
        }
        for (StateTransition stateTrans : path.getTransitions()) {
            score += stateTrans.getScore();
        }
        path.setScore(score);
    }


}
