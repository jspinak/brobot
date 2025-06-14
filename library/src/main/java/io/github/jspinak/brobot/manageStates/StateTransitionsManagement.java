package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.report.log.ActionLogger;
import io.github.jspinak.brobot.report.log.AutomationSession;
import io.github.jspinak.brobot.report.Output;
import io.github.jspinak.brobot.report.Report;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
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

    private final PathFinder pathFinder;
    private final AllStatesInProjectService allStatesInProjectService;
    private final StateMemory stateMemory;
    private final TraversePaths traversePaths;
    private final PathManager pathManager;
    private final ActionLogger actionLogger;
    private final AutomationSession automationSession;

    Set<Long> activeStates;

    public StateTransitionsManagement(PathFinder pathFinder, AllStatesInProjectService allStatesInProjectService,
                                      StateMemory stateMemory, TraversePaths traversePaths, PathManager pathManager,
                                      ActionLogger actionLogger, AutomationSession automationSession) {
        this.pathFinder = pathFinder;
        this.allStatesInProjectService = allStatesInProjectService;
        this.stateMemory = stateMemory;
        this.traversePaths = traversePaths;
        this.pathManager = pathManager;
        this.actionLogger = actionLogger;
        this.automationSession = automationSession;
    }

    public boolean openState(String stateName) {
        Report.format("Open State %s\n", stateName);
        Long stateToOpen = allStatesInProjectService.getStateId(stateName);
        if (stateToOpen == null) {
            Report.println(Output.fail+" Target state not found.");
            return false;
        }
        return openState(stateToOpen);
    }

    public boolean openState(Long stateToOpen) {
        Report.format("Open State %s\n", allStatesInProjectService.getStateName(stateToOpen));
        String sessionId = automationSession.getCurrentSessionId();
        Instant startTime = Instant.now();
        activeStates = stateMemory.getActiveStates();

        Optional<State> targetState = allStatesInProjectService.getState(stateToOpen);
        if (targetState.isEmpty()) {
            Report.println(Output.fail+" Target state not found.");
            return false;
        }
        // Log transition attempt start
        String transitionDescription = "Transition from " + stateMemory.getActiveStateNamesAsString() +
                " to " + targetState.get().getName();
        actionLogger.logObservation(automationSession.getCurrentSessionId(), "Transition start:", transitionDescription, "info");

        // we find the paths once, and then reuse these paths when needed
        Paths paths = pathFinder.getPathsToState(activeStates, stateToOpen);
        boolean success = recursePaths(paths, stateToOpen, sessionId);
        Duration duration = Duration.between(startTime, Instant.now());

        // Log transition attempt end
        Set<State> activeStatesSet = allStatesInProjectService.findSetById(activeStates);
        actionLogger.logStateTransition(
                sessionId, // no duration for start log
                activeStatesSet,
                Collections.singleton(targetState.get()),
                allStatesInProjectService.findSetById(activeStates),
                success,
                duration.toMillis()
        );

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
    private boolean recursePaths(Paths paths, Long stateToOpen, String sessionId) {
        if (paths.isEmpty()) return false;
        if (activeStates.contains(stateToOpen) && traversePaths.finishTransition(stateToOpen)) return true;
        if (traversePaths.traverse(paths.getPaths().getFirst())) return true; // false if a transition fails
        activeStates = stateMemory.getActiveStates(); // may have changed after traversal
        return recursePaths(
                pathManager.getCleanPaths(activeStates, paths, traversePaths.getFailedTransitionStartState()),
                stateToOpen,
                sessionId);
    }

}
