package io.github.jspinak.brobot.manageStates;

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

    private String stateName;
    private StateTransition transitionFinish;
    private Map<String, StateTransition> transitions;
    /**
     * When set, the same variable in a Transition takes precedence over this one.
     * Only applies to FromTransitions.
     */
    private boolean staysVisibleAfterTransition = false; // the same variable in a Transition takes precedence

    public boolean open(String stateToOpen) {
        System.out.format("\n\n%s -> %s\n",this.stateName, stateToOpen);
        if (!transitions.containsKey(stateToOpen)) return false;
        return transitions.get(stateToOpen).getTransitionFunction().getAsBoolean();
    }

    public Optional<StateTransition> getStateTransition(String to) {
        return Optional.ofNullable(transitions.get(to));
    }

    public Optional<BooleanSupplier> getTransitionFunction(String to) {
        Optional<StateTransition> stateTransition = Optional.ofNullable(transitions.get(to));
        if (stateTransition.isEmpty()) return Optional.empty();
        return Optional.of(stateTransition.get().getTransitionFunction());
    }

    public boolean stateStaysVisible(String toState) {
        Optional<StateTransition> stateTransition = getStateTransition(toState);
        if (stateTransition.isEmpty()) return false; // couldn't find the Transition, return value not important
        StateTransition.StaysVisible localStaysVisible = stateTransition.get().getStaysVisibleAfterTransition();
        if (localStaysVisible == StateTransition.StaysVisible.NONE) return staysVisibleAfterTransition;
        else return localStaysVisible == StateTransition.StaysVisible.TRUE;
    }

    public void addTransition(StateTransition transition) {
        for (String stateName : transition.getActivate()) transitions.put(stateName, transition);
    }

    public void addTransition(BooleanSupplier transition, String... toStates) {
        StateTransition stateTransition = new StateTransition.Builder()
                .setFunction(transition)
                .addToActivate(toStates)
                .build();
        addTransition(stateTransition);
    }

    public static class Builder {

        private String stateName;
        private StateTransition transitionFinish = new StateTransition.Builder().build();
        private Map<String, StateTransition> transitions = new HashMap<>();
        private boolean staysVisibleAfterTransition = false;

        public Builder(String stateName) {
            this.stateName = stateName;
        }

        public Builder addTransition(BooleanSupplier transition, String... toStates) {
            StateTransition stateTransition = new StateTransition.Builder()
                    .setFunction(transition)
                    .addToActivate(toStates)
                    .build();
            return addTransition(stateTransition);
        }

        public Builder addTransition(StateTransition transition) {
            for (String stateName : transition.getActivate()) transitions.put(stateName, transition);
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
