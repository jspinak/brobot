package io.github.jspinak.brobot.manageStates;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.jspinak.brobot.dsl.ActionDefinition;
import lombok.Data;

import java.util.*;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class StateTransitions {

    private String stateName; // for setup, must be unique
    private Long stateId; // for runtime
    private IStateTransition transitionFinish;
    private Map<Long, ActionDefinitionStateTransition> actionDefinitionTransitions = new HashMap<>();
    private List<IStateTransition> transitions = new ArrayList<>(); // for use at runtime
    /**
     * When set, the same variable in a Transition takes precedence over this one.
     * Only applies to FromTransitions.
     */
    private boolean staysVisibleAfterTransition = false; // the same variable in a Transition takes precedence

    public Optional<IStateTransition> getTransitionFunctionByActivatedStateId(Long to) {
        if (to == null)
            return Optional.empty();
        if (to.equals(stateId))
            return Optional.of(transitionFinish);
        for (IStateTransition transition : transitions) {
            if (transition.getActivate().contains(to))
                return Optional.of(transition);
        }
        return Optional.empty();
    }

    public boolean stateStaysVisible(Long toState) {
        Optional<IStateTransition> stateTransition = getTransitionFunctionByActivatedStateId(toState);
        if (stateTransition.isEmpty())
            return false; // couldn't find the Transition, return value not important
        IStateTransition.StaysVisible localStaysVisible = stateTransition.get().getStaysVisibleAfterTransition();
        if (localStaysVisible == IStateTransition.StaysVisible.NONE)
            return staysVisibleAfterTransition;
        else
            return localStaysVisible == IStateTransition.StaysVisible.TRUE;
    }

    public void addTransition(JavaStateTransition transition) {
        transitions.add(transition);
    }

    public void addTransition(ActionDefinitionStateTransition transition) {
        for (Long id : transition.getActivate()) {
            transitions.add(transition);
            actionDefinitionTransitions.put(id, transition);
        }
    }

    public void addTransition(BooleanSupplier transition, String... toStates) {
        JavaStateTransition javaStateTransition = new JavaStateTransition.Builder()
                .setFunction(transition)
                .addToActivate(toStates)
                .build();
        addTransition(javaStateTransition);
    }

    public Optional<ActionDefinition> getActionDefinition(Long toState) {
        Optional<IStateTransition> transition = getTransitionFunctionByActivatedStateId(toState);
        if (transition.isEmpty() || !(transition.get() instanceof ActionDefinitionStateTransition))
            return Optional.empty();
        return transition.get().getActionDefinitionOptional();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (stateId != null) stringBuilder.append("id=").append(stateId).append(", ");
        if (stateName != null) stringBuilder.append("from=").append(stateName).append(" ");
        stringBuilder.append("to=");
        transitions.forEach(transition -> {
            if (transition instanceof ActionDefinitionStateTransition) {
                stringBuilder.append(transition.getActivate());
            } else {
                stringBuilder.append(((JavaStateTransition) transition).getActivateNames());
                stringBuilder.append(",");
            }
        });
        return stringBuilder.toString();
    }

    public static class Builder {

        private final String stateName;
        private IStateTransition transitionFinish = new JavaStateTransition.Builder().build();
        private final List<IStateTransition> transitions = new ArrayList<>();
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
            transitions.add(transition);
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
            stateTransitions.transitions = transitions;
            stateTransitions.staysVisibleAfterTransition = staysVisibleAfterTransition;
            return stateTransitions;
        }
    }

}
