package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.primatives.enums.SpecialStateType;
import io.github.jspinak.brobot.reports.ANSI;
import io.github.jspinak.brobot.reports.Report;
import io.github.jspinak.brobot.services.StateTransitionsInProjectService;
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
    private StateTransitionsInProjectService stateTransitionsInProjectService;
    private final TransitionBooleanSupplierPackager transitionBooleanSupplierPackager;

    private Long failedTransitionStartState = SpecialStateType.NULL.getId();

    public TraversePaths(DoTransition doTransition, StateTransitionsInProjectService stateTransitionsInProjectService,
                         TransitionBooleanSupplierPackager transitionBooleanSupplierPackager) {
        this.doTransition = doTransition;
        this.stateTransitionsInProjectService = stateTransitionsInProjectService;
        this.transitionBooleanSupplierPackager = transitionBooleanSupplierPackager;
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

    boolean finishTransition(Long stateToOpen) {
        Optional<StateTransitions> optToStateTransitions = stateTransitionsInProjectService.getTransitions(stateToOpen);
        if (optToStateTransitions.isEmpty()) {
            Report.print(Report.OutputLevel.HIGH,"'to state' " + stateToOpen + " not a valid transition", ANSI.RED);
            return false;
        }
        return transitionBooleanSupplierPackager.getAsBoolean(optToStateTransitions.get().getTransitionFinish());
    }

}
