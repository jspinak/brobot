package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.actions.customActions.CommonActions;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
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

    public StateTransitions basicTransition(String stateName, String toState, StateImage toStateImage) {
        return new StateTransitions.Builder(stateName)
                .addTransitionFinish(() -> commonActions.findState(1, stateName))
                .addTransition(() -> commonActions.find(1, toStateImage), toState)
                .build();
    }
}
