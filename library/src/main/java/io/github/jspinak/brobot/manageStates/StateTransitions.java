package io.github.jspinak.brobot.manageStates;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

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

    private String stateName; // for reference
    private Long stateId;
    private IStateTransition transitionFinish;
    private Map<String, IStateTransition> transitionsByName = new HashMap<>(); // for initial setup
    private Map<Long, IStateTransition> transitions = new HashMap<>(); // for use at runtime
    /**
     * When set, the same variable in a Transition takes precedence over this one.
     * Only applies to FromTransitions.
     */
    private boolean staysVisibleAfterTransition = false; // the same variable in a Transition takes precedence

    public void convertNamesToIds(Function<String, Long> nameToIdConverter) {
        this.stateId = nameToIdConverter.apply(stateName);
        this.transitions = new HashMap<>();
        for (Map.Entry<String, IStateTransition> entry : transitionsByName.entrySet()) {
            Long toStateId = nameToIdConverter.apply(entry.getKey());
            IStateTransition transition = entry.getValue();
            if (transition instanceof JavaStateTransition) {
                ((JavaStateTransition) transition).convertNamesToIds(nameToIdConverter);
            }
            transitions.put(toStateId, transition);
        }
    }

    public Optional<IStateTransition> getStateTransition(Long to) {
        return Optional.ofNullable(transitions.get(to));
    }

    public Optional<IStateTransition> getTransitionFunction(Long to) {
        return Optional.ofNullable(transitions.get(to));
    }

    public boolean stateStaysVisible(Long toState) {
        Optional<IStateTransition> stateTransition = getStateTransition(toState);
        if (stateTransition.isEmpty()) return false; // couldn't find the Transition, return value not important
        IStateTransition.StaysVisible localStaysVisible = stateTransition.get().getStaysVisibleAfterTransition();
        if (localStaysVisible == IStateTransition.StaysVisible.NONE) return staysVisibleAfterTransition;
        else return localStaysVisible == IStateTransition.StaysVisible.TRUE;
    }

    public void addTransition(JavaStateTransition transition) {
        for (String stateName : transition.getActivateNames()) transitionsByName.put(stateName, transition);
    }

    public void addTransition(BooleanSupplier transition, String... toStates) {
        JavaStateTransition javaStateTransition = new JavaStateTransition.Builder()
                .setFunction(transition)
                .addToActivate(toStates)
                .build();
        addTransition(javaStateTransition);
    }

    public static class Builder {

        private final String stateName;
        private IStateTransition transitionFinish = new JavaStateTransition.Builder().build();
        private final Map<String, IStateTransition> transitionsByName = new HashMap<>();
        private boolean staysVisibleAfterTransition = false;

        public Builder(String stateName) {
            this.stateName = stateName;
        }

        public Builder addTransition(BooleanSupplier transition, String... toStates) {
            JavaStateTransition javaStateTransition = new JavaStateTransition.Builder()
                    .setFunction(transition)
                    .addToActivate(toStates)
                    .build();
            return addTransition(javaStateTransition);
        }

        public Builder addTransition(JavaStateTransition transition) {
            for (String stateName : transition.getActivateNames()) transitionsByName.put(stateName, transition);
            return this;
        }

        public Builder addTransitionFinish(BooleanSupplier transitionFinish) {
            this.transitionFinish = new JavaStateTransition.Builder().setFunction(transitionFinish).build();
            return this;
        }

        public Builder addTransitionFinish(JavaStateTransition transitionFinish) {
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
            stateTransitions.transitionsByName = transitionsByName;
            stateTransitions.staysVisibleAfterTransition = staysVisibleAfterTransition;
            return stateTransitions;
        }
    }

}
