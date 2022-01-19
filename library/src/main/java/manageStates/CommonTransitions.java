package manageStates;

import com.brobot.multimodule.actions.customActions.CommonActions;
import com.brobot.multimodule.database.state.stateObject.stateImageObject.StateImageObject;
import com.brobot.multimodule.primatives.enums.StateEnum;
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
