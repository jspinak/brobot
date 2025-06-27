package io.github.jspinak.brobot.model.transition;

import io.github.jspinak.brobot.action.composite.chains.ActionFacade;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;

import org.springframework.stereotype.Component;

/**
 * This class should make it simpler to create common StateTransitions.
 */
@Component
public class StandardTransitions {

    private ActionFacade commonActions;

    public StandardTransitions(ActionFacade commonActions) {
        this.commonActions = commonActions;
    }

    public StateTransitions basicTransition(String stateName, String toState, StateImage toStateImage) {
        return new StateTransitions.Builder(stateName)
                .addTransitionFinish(() -> commonActions.findState(1, stateName))
                .addTransition(() -> commonActions.find(1, toStateImage), toState)
                .build();
    }
}
