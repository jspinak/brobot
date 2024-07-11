package io.github.jspinak.brobot.manageStates;

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

    // when set to NONE, the StaysVisible variable in the corresponding StateTransitions object will be used.
    public enum StaysVisible {
        NONE, TRUE, FALSE
    }

    private BooleanSupplier transitionFunction;
    /**
     * When set, takes precedence over the same variable in StateTransitions.
     * Only applies to FromTransitions.
     */
    private StaysVisible staysVisibleAfterTransition;
    private Set<String> activate;
    private Set<String> exit;
    private int score = 0; // larger path scores discourage taking a path with this transition
    private int timesSuccessful = 0;

    public boolean getAsBoolean() {
        return transitionFunction.getAsBoolean();
    }

    public static class Builder {
        private BooleanSupplier transitionFunction = () -> false;
        private StaysVisible staysVisibleAfterTransition = StaysVisible.NONE;
        private Set<String> activate = new HashSet<>();
        private Set<String> exit = new HashSet<>();
        private int score = 0;

        public Builder setFunction(BooleanSupplier booleanSupplier) {
            this.transitionFunction = booleanSupplier;
            return this;
        }

        public Builder setStaysVisibleAfterTransition(StaysVisible staysVisibleAfterTransition) {
            this.staysVisibleAfterTransition = staysVisibleAfterTransition;
            return this;
        }

        public Builder setStaysVisibleAfterTransition(boolean staysVisibleAfterTransition) {
            if (staysVisibleAfterTransition) this.staysVisibleAfterTransition = StaysVisible.TRUE;
            else this.staysVisibleAfterTransition = StaysVisible.FALSE;
            return this;
        }

        public Builder addToActivate(String... stateNames) {
            this.activate.addAll(List.of(stateNames));
            return this;
        }

        public Builder addToExit(String... stateNames) {
            this.exit.addAll(List.of(stateNames));
            return this;
        }

        public Builder setScore(int score) {
            this.score = score;
            return this;
        }

        public StateTransition build() {
            StateTransition stateTransition = new StateTransition();
            stateTransition.transitionFunction = transitionFunction;
            stateTransition.staysVisibleAfterTransition = staysVisibleAfterTransition;
            stateTransition.activate = activate;
            stateTransition.exit = exit;
            stateTransition.score = score;
            return stateTransition;
        }
    }
}
