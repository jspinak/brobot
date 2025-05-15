package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.dsl.ActionDefinition;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;

/**
 * After a successful transition (both 'from' and 'to' Transitions):
 *   'activate' holds all States to make active
 *   'exit' holds all States to deactivate
 */
@Getter
@Setter
public class JavaStateTransition implements IStateTransition {
    private String type = "java";
    private BooleanSupplier transitionFunction;

    private StaysVisible staysVisibleAfterTransition;
    /*
    Use names instead of IDs for coding these by hand since state ids are set at runtime.
     */
    private Set<String> activateNames = new HashSet<>();
    private Set<String> exitNames = new HashSet<>();;
    private Set<Long> activate = new HashSet<>();;
    private Set<Long> exit = new HashSet<>();;
    private int score = 0;
    private int timesSuccessful = 0;

    @Override
    public Optional<ActionDefinition> getActionDefinitionOptional() {
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "activate=" + activateNames + ", exit=" + exitNames + '}';
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

        public JavaStateTransition build() {
            JavaStateTransition javaStateTransition = new JavaStateTransition();
            javaStateTransition.transitionFunction = transitionFunction;
            javaStateTransition.staysVisibleAfterTransition = staysVisibleAfterTransition;
            javaStateTransition.activateNames = activate;
            javaStateTransition.exitNames = exit;
            javaStateTransition.score = score;
            return javaStateTransition;
        }
    }
}
