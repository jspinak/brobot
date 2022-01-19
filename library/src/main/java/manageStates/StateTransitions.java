package manageStates;

import com.brobot.multimodule.primatives.enums.StateEnum;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;

/**
 * StateTransitions hold all the Transitions for a State.
 * The 'transitionFinish' is the ToTransition that is performed after
 * a successful FromTransition from another State going to this State.
 * This StateTransitions can also have FromTransitions that start a
 * transition to another State.
 *
 * By default, this State will exit after a successful transition,
 * but this can be changed with the variable 'staysVisibleAfterTransition'.
 */
@Data
public class StateTransitions {

    private StateEnum stateName;
    private StateTransition transitionFinish;
    private Map<StateEnum, StateTransition> transitions;
    /**
     * When set, the same variable in a Transition takes precedence over this one.
     * Only applies to FromTransitions.
     */
    private boolean staysVisibleAfterTransition = false; // the same variable in a Transition takes precedence

    public boolean open(StateEnum stateToOpen) {
        System.out.format("\n\n%s -> %s\n",this.stateName, stateToOpen);
        if (!transitions.containsKey(stateToOpen)) return false;
        return transitions.get(stateToOpen).getTransitionFunction().getAsBoolean();
    }

    public Optional<StateTransition> getStateTransition(StateEnum to) {
        return Optional.ofNullable(transitions.get(to));
    }

    public Optional<BooleanSupplier> getTransitionFunction(StateEnum to) {
        Optional<StateTransition> stateTransition = Optional.ofNullable(transitions.get(to));
        if (stateTransition.isEmpty()) return Optional.empty();
        return Optional.of(stateTransition.get().getTransitionFunction());
    }

    public boolean stateStaysVisible(StateEnum toStateEnum) {
        Optional<StateTransition> stateTransition = getStateTransition(toStateEnum);
        if (stateTransition.isEmpty()) return false; // couldn't find the Transition, return value not important
        StateTransition.StaysVisible localStaysVisible = stateTransition.get().getStaysVisibleAfterTransition();
        if (localStaysVisible == StateTransition.StaysVisible.NONE) return staysVisibleAfterTransition;
        else return localStaysVisible == StateTransition.StaysVisible.TRUE;
    }

    public static class Builder {

        private StateEnum stateName;
        private StateTransition transitionFinish = new StateTransition.Builder().build();
        private Map<StateEnum, StateTransition> transitions = new HashMap<>();
        private boolean staysVisibleAfterTransition = false;

        public Builder(StateEnum stateName) {
            this.stateName = stateName;
        }

        public Builder addTransition(BooleanSupplier transition, StateEnum... toStates) {
            StateTransition stateTransition = new StateTransition.Builder()
                    .setFunction(transition)
                    .addToActivate(toStates)
                    .build();
            return addTransition(stateTransition);
        }

        public Builder addTransition(StateTransition transition) {
            for (StateEnum stateEnum : transition.getActivate()) transitions.put(stateEnum, transition);
            return this;
        }

        public Builder addTransitionFinish(BooleanSupplier transitionFinish) {
            this.transitionFinish = new StateTransition.Builder().setFunction(transitionFinish).build();
            return this;
        }

        public Builder addTransitionFinish(StateTransition transitionFinish) {
            this.transitionFinish = transitionFinish;
            return this;
        }

        public Builder setStaysVisibleAfterTransition(boolean staysVisibleAfterTransition) {
            this.staysVisibleAfterTransition = staysVisibleAfterTransition;
            return this;
        }

        public StateTransitions build() {
            StateTransitions stateTransitions = new StateTransitions();
            stateTransitions.stateName = stateName;
            stateTransitions.transitionFinish = transitionFinish;
            stateTransitions.transitions = transitions;
            stateTransitions.staysVisibleAfterTransition = staysVisibleAfterTransition;
            return stateTransitions;
        }
    }

}
