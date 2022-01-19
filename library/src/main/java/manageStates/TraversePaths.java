package manageStates;

import com.brobot.multimodule.primatives.enums.StateEnum;
import com.brobot.multimodule.reports.ANSI;
import com.brobot.multimodule.reports.Report;
import com.brobot.multimodule.services.StateTransitionsService;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.brobot.multimodule.database.state.NullState.Enum.NULL;

/**
 * Moves through the Paths to reach the target State.
 */
@Component
@Getter
public class TraversePaths {

    private DoTransition doTransition;
    private StateTransitionsService stateTransitionsService;

    private StateEnum failedTransitionStartState = NULL;

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

    boolean finishTransition(StateEnum stateToOpen) {
        Optional<StateTransitions> optToStateTransitions = stateTransitionsService.getTransitions(stateToOpen);
        if (optToStateTransitions.isEmpty()) {
            Report.print(Report.OutputLevel.HIGH,"'to state' " + stateToOpen + " not a valid transition", ANSI.RED);
            return false;
        }
        return optToStateTransitions.get().getTransitionFinish().getAsBoolean();
    }

}
