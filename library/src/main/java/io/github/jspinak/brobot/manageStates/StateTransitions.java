package io.github.jspinak.brobot.manageStates;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.jspinak.brobot.dsl.ActionDefinition;
import lombok.Data;

import java.util.*;
import java.util.function.BooleanSupplier;

/**
 * Container for all transitions associated with a specific State in the Brobot framework.
 * 
 * <p>StateTransitions is a key component of the state structure (Ω), defining the edges 
 * in the state graph that enable navigation between GUI configurations. It encapsulates 
 * both incoming transitions (how to finalize arrival at this state) and outgoing 
 * transitions (how to navigate from this state to others).</p>
 * 
 * <p>Transition types:
 * <ul>
 *   <li><b>Transition Finish</b>: Actions performed when arriving at this state from another 
 *       state, ensuring the GUI is in the expected configuration</li>
 *   <li><b>Outgoing Transitions</b>: Collection of transitions that can navigate from this 
 *       state to other states in the application</li>
 * </ul>
 * </p>
 * 
 * <p>Key features:
 * <ul>
 *   <li>Supports multiple transition types (Java functions, Action definitions)</li>
 *   <li>Handles state visibility control during transitions</li>
 *   <li>Enables dynamic path finding by providing transition options</li>
 *   <li>Maintains bidirectional navigation information</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, StateTransitions transform implicit navigation knowledge 
 * into explicit, executable paths through the GUI. This enables the framework to automatically 
 * find routes between states and recover from unexpected situations by recalculating paths.</p>
 * 
 * @since 1.0
 * @see State
 * @see IStateTransition
 * @see JavaStateTransition
 * @see ActionDefinitionStateTransition
 * @see PathFinder
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
