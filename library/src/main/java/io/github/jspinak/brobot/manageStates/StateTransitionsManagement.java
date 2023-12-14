package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.reports.Output;
import io.github.jspinak.brobot.reports.Report;
import io.github.jspinak.brobot.services.StateTransitionsService;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * We want to go to a specific State, the target State. To do so, we search the States for
 * Paths to the target State. From these Paths, we choose the one with the lowest Score, and
 * try to traverse the Path. If this is not successful we had a problem with a transition, and
 * we assume that this problem will persist. We won't try this transition again until all other
 * Paths have been tried. However, we may be now at a different State than before. There is no
 * need to find new Paths, but we can truncate some Paths and eliminate Paths that don't
 * contain the current start State. Active States are always up-to-date in the StateMemory.
 */
@Component
public class StateTransitionsManagement {

    private StateTransitionsService stateTransitionsService;
    private PathFinder pathFinder;
    private StateMemory stateMemory;
    private TraversePaths traversePaths;
    private PathManager pathManager;

    Set<String> activeStates;

    public StateTransitionsManagement(StateTransitionsService stateTransitionsService, PathFinder pathFinder,
                                      StateMemory stateMemory, TraversePaths traversePaths, PathManager pathManager) {
        this.stateTransitionsService = stateTransitionsService;
        this.pathFinder = pathFinder;
        this.stateMemory = stateMemory;
        this.traversePaths = traversePaths;
        this.pathManager = pathManager;
    }

    public boolean openState(String stateToOpen) {
        Report.format("Open State %s\n", stateToOpen);
        activeStates = stateMemory.getActiveStates();
        // we find the paths once, and then reuse these paths when needed
        Paths paths = pathFinder.getPathsToState(activeStates, stateToOpen);
        boolean success = recursePaths(paths, stateToOpen);
        if (!success) Report.println(Output.fail+" All paths tried, open failed.");
        Report.println("Active States: " + activeStates +"\n");
        return success;
    }

    /**
     * If there are no more Paths, return false. If the target State is an active State,
     * and it successfully transitions to the target State, return true. Otherwise, take the
     * first Path (which has the lowest Score) and try to traverse it until the target State
     * is reached. If this succeeds, return true. If it fails, trim the reachable Paths,
     * remove unreachable Paths, and recursively call this method again.
     * <p>
     * How it deals with failed Transitions:
     * The desired way of dealing with failed Transitions may differ based on the application, but since
     * Brobot has enough flexibility to deal with these differences in the Transitions,
     * there is no interface that allows for user-defined 'recursePaths' methods. Basically, if you want
     * to attempt a Transition 5 times until you give up, you should specify this in the specific
     * Transition. This provides a more granular approach since every Transition can be different.
     * Here, a failed Transition is assumed to always fail, and therefore Path objects with this Transition
     * are removed from the available Paths and not tried again.
     */
    private boolean recursePaths(Paths paths, String stateToOpen) {
        if (paths.isEmpty()) return false;
        if (activeStates.contains(stateToOpen) && traversePaths.finishTransition(stateToOpen)) return true;
        if (traversePaths.traverse(paths.getPaths().get(0))) return true; // false if a transition fails
        activeStates = stateMemory.getActiveStates(); // may have changed after traverse
        return recursePaths(
                pathManager.getCleanPaths(activeStates, paths, traversePaths.getFailedTransitionStartState()),
                stateToOpen);
    }

}
