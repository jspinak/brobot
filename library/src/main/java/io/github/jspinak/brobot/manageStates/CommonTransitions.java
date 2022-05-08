package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.actions.customActions.CommonActions;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.primatives.enums.StateEnum;
import org.springframework.stereotype.Component;

/**
 * This class should make it simpler to create common StateTransitions.
 */
@Component
public class CommonTransitions {

    private CommonActions commonActions;

    public CommonTransitions(CommonActions commonActions) {
        this.commonActions = commonActions;
    }

    public StateTransitions basicTransition(StateEnum stateEnum, StateEnum toState, StateImageObject toStateImage) {
        return new StateTransitions.Builder(stateEnum)
                .addTransitionFinish(() -> commonActions.findState(1, stateEnum))
                .addTransition(() -> commonActions.find(1, toStateImage), toState)
                .build();
    }
}
