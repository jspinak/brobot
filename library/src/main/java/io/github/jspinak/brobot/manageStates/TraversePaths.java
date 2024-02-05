package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.reports.ANSI;
import io.github.jspinak.brobot.reports.Report;
import io.github.jspinak.brobot.services.StateTransitionsService;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Moves through the Paths to reach the target State.
 */
@Component
@Getter
public class TraversePaths {

    private DoTransition doTransition;
    private StateTransitionsService stateTransitionsService;

    private String failedTransitionStartState = "null";

    public TraversePaths(DoTransition doTransition, StateTransitionsService stateTransitionsService) {
        this.doTransition = doTransition;
        this.stateTransitionsService = stateTransitionsService;
    }

    public boolean traverse(Path path) {
        for (int i=0; i<path.size()-1; i++) {
            if (!doTransition.go(path.get(i), path.get(i + 1))) {
                failedTransitionStartState = path.get(i);
                return false;
            }
        }
        return true;
    }

    boolean finishTransition(String stateToOpen) {
        Optional<StateTransitions> optToStateTransitions = stateTransitionsService.getTransitions(stateToOpen);
        if (optToStateTransitions.isEmpty()) {
            Report.print(Report.OutputLevel.HIGH,"'to state' " + stateToOpen + " not a valid transition", ANSI.RED);
            return false;
        }
        return optToStateTransitions.get().getTransitionFinish().getAsBoolean();
    }

}
