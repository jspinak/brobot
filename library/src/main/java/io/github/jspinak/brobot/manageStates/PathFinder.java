package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.database.state.state.State;
import io.github.jspinak.brobot.primatives.enums.StateEnum;
import io.github.jspinak.brobot.reports.Output;
import io.github.jspinak.brobot.reports.Report;
import io.github.jspinak.brobot.services.StateService;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Finds all paths from a set of start States to a target State.
 * The Path list is sorted in ascending order for Path score.
 */
@Component
public class PathFinder {

    private StateTransitionsJointTable stateTransitionsJointTable;
    private StateService stateService;

    private Set<StateEnum> startStates;
    private List<Path> pathList;

    public PathFinder(StateTransitionsJointTable stateTransitionsJointTable, StateService stateService) {
        this.stateTransitionsJointTable = stateTransitionsJointTable;
        this.stateService = stateService;
    }

    public Paths getPathsToState(Set<StateEnum> startStates, StateEnum targetState) {
        Report.println("Find path: " + startStates + " -> " + targetState);
        this.startStates = startStates;
        pathList = new ArrayList<>();
        recursePath(new Path(), targetState);
        if (pathList.isEmpty()) Report.println(Output.fail + "Path to state not found.");
        pathList.sort(Comparator.comparing(Path::getScore));
        Paths paths = new Paths(pathList);
        paths.print();
        return paths;
    }

    private void recursePath(Path path, StateEnum stateInFocus) {
        if (!path.contains(stateInFocus)) {
            path.add(stateInFocus);
            if (startStates.contains(stateInFocus)) { // a path is found
                Path successfulPath = path.getCopy();
                successfulPath.reverse();
                setPathScore(successfulPath);
                pathList.add(successfulPath);
            } else { // continue searching
                Set<StateEnum> parentStates = stateTransitionsJointTable.getStatesWithTransitionsTo(stateInFocus);
                for (StateEnum newState : parentStates) {
                    recursePath(path, newState);
                }
            }
        }
        if (path.get(path.size() - 1) == stateInFocus) path.remove(stateInFocus); // otherwise it's circular
    }

    private void setPathScore(Path path) {
        int score = 0;
        for (StateEnum stateEnum : path.getPath()) {
            Optional<State> state = stateService.findByName(stateEnum);
            if (state.isPresent()) score += state.get().getPathScore();
        }
        path.setScore(score);
    }


}
