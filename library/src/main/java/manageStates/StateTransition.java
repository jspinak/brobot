package manageStates;

import com.brobot.multimodule.primatives.enums.StateEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;

/**
 * After a successful transition (both 'from' and 'to' Transitions):
 *   'activate' holds all States to make active
 *   'exit' holds all States to deactivate
 */
@Getter
@Setter
public class StateTransition {

    public enum StaysVisible {
        NONE, TRUE, FALSE
    }

    private BooleanSupplier transitionFunction;
    /**
     * When set, takes precedence over the same variable in StateTransitions.
     * Only applies to FromTransitions.
     */
    private StaysVisible staysVisibleAfterTransition;
    private Set<StateEnum> activate;
    private Set<StateEnum> exit;

    public boolean getAsBoolean() {
        return transitionFunction.getAsBoolean();
    }

    public static class Builder {
        private BooleanSupplier transitionFunction = () -> false;
        private StaysVisible staysVisibleAfterTransition = StaysVisible.NONE;
        private Set<StateEnum> activate = new HashSet<>();
        private Set<StateEnum> exit = new HashSet<>();

        public Builder setFunction(BooleanSupplier booleanSupplier) {
            this.transitionFunction = booleanSupplier;
            return this;
        }

        public Builder setStaysVisibleAfterTransition(StaysVisible staysVisibleAfterTransition) {
            this.staysVisibleAfterTransition = staysVisibleAfterTransition;
            return this;
        }

        public Builder addToActivate(StateEnum... stateEnums) {
            this.activate.addAll(List.of(stateEnums));
            return this;
        }

        public Builder addToExit(StateEnum... stateEnums) {
            this.exit.addAll(List.of(stateEnums));
            return this;
        }

        public StateTransition build() {
            StateTransition stateTransition = new StateTransition();
            stateTransition.transitionFunction = transitionFunction;
            stateTransition.staysVisibleAfterTransition = staysVisibleAfterTransition;
            stateTransition.activate = activate;
            stateTransition.exit = exit;
            return stateTransition;
        }
    }
}
