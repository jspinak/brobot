package io.github.jspinak.brobot.navigation.transition;

import java.util.*;
import java.util.function.BooleanSupplier;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.runner.dsl.model.TaskSequence;

import lombok.Data;

/**
 * Container for all transitions associated with a specific State in the Brobot framework.
 *
 * <p>StateTransitions is a key component of the state structure (Ω), defining the edges in the
 * state graph that enable navigation between GUI configurations. It encapsulates both incoming
 * transitions (how to finalize arrival at this state) and outgoing transitions (how to navigate
 * from this state to others).
 *
 * <p>Transition types:
 *
 * <ul>
 *   <li><b>Transition Finish</b>: Actions performed when arriving at this state from another state,
 *       ensuring the GUI is in the expected configuration
 *   <li><b>Outgoing Transitions</b>: Collection of transitions that can navigate from this state to
 *       other states in the application
 * </ul>
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Supports multiple transition types (Java functions, Action definitions)
 *   <li>Handles state visibility control during transitions
 *   <li>Enables dynamic path finding by providing transition options
 *   <li>Maintains bidirectional navigation information
 * </ul>
 *
 * <p>In the model-based approach, StateTransitions transform implicit navigation knowledge into
 * explicit, executable paths through the GUI. This enables the framework to automatically find
 * routes between states and recover from unexpected situations by recalculating paths.
 *
 * @since 1.0
 * @see State
 * @see StateTransition
 * @see JavaStateTransition
 * @see TaskSequenceStateTransition
 * @see PathFinder
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StateTransitions {

    private String stateName; // for setup, must be unique
    private Long stateId; // for runtime
    private StateTransition transitionFinish;
    private Map<Long, TaskSequenceStateTransition> actionDefinitionTransitions = new HashMap<>();
    private List<StateTransition> transitions = new ArrayList<>(); // for use at runtime

    /**
     * When set, the same variable in a Transition takes precedence over this one. Only applies to
     * FromTransitions.
     */
    private boolean staysVisibleAfterTransition =
            false; // the same variable in a Transition takes precedence

    /**
     * Finds the transition that activates a specific target state.
     *
     * <p>Searches through all transitions to find one that includes the target state in its
     * activation list. If the target is this state itself, returns the transitionFinish.
     *
     * @param to ID of the target state to find a transition for
     * @return Optional containing the transition if found, empty otherwise
     */
    public Optional<StateTransition> getTransitionFunctionByActivatedStateId(Long to) {
        if (to == null) return Optional.empty();
        if (to.equals(stateId)) return Optional.of(transitionFinish);
        for (StateTransition transition : transitions) {
            if (transition.getActivate().contains(to)) return Optional.of(transition);
        }
        return Optional.empty();
    }

    /**
     * Determines if this state remains visible after transitioning to another state.
     *
     * <p>Visibility is determined by:
     *
     * <ol>
     *   <li>Transition-specific setting (if not NONE)
     *   <li>State-level default setting (if transition setting is NONE)
     * </ol>
     *
     * @param toState ID of the target state being transitioned to
     * @return true if this state stays visible, false otherwise
     */
    public boolean stateStaysVisible(Long toState) {
        Optional<StateTransition> stateTransition =
                getTransitionFunctionByActivatedStateId(toState);
        if (stateTransition.isEmpty())
            return false; // couldn't find the Transition, return value not important
        StateTransition.StaysVisible localStaysVisible =
                stateTransition.get().getStaysVisibleAfterTransition();
        if (localStaysVisible == StateTransition.StaysVisible.NONE)
            return staysVisibleAfterTransition;
        else return localStaysVisible == StateTransition.StaysVisible.TRUE;
    }

    /**
     * Adds a Java-based transition to this state's outgoing transitions.
     *
     * @param transition JavaStateTransition to add
     */
    public void addTransition(JavaStateTransition transition) {
        transitions.add(transition);
    }

    /**
     * Adds an ActionDefinition-based transition to this state's outgoing transitions.
     *
     * <p>Also maintains a lookup map for quick access by target state ID.
     *
     * @param transition ActionDefinitionStateTransition to add
     */
    public void addTransition(TaskSequenceStateTransition transition) {
        for (Long id : transition.getActivate()) {
            transitions.add(transition);
            actionDefinitionTransitions.put(id, transition);
        }
    }

    /**
     * Convenience method to add a simple function-based transition.
     *
     * <p>Creates a JavaStateTransition with the provided function and target states.
     *
     * @param transition Function containing transition logic
     * @param toStates Names of states to activate on success
     */
    public void addTransition(BooleanSupplier transition, String... toStates) {
        JavaStateTransition javaStateTransition =
                new JavaStateTransition.Builder()
                        .setFunction(transition)
                        .addToActivate(toStates)
                        .build();
        addTransition(javaStateTransition);
    }

    /**
     * Retrieves the ActionDefinition for transitioning to a specific state.
     *
     * <p>Only returns a value if the transition is ActionDefinition-based.
     *
     * @param toState ID of the target state
     * @return Optional containing ActionDefinition if found and applicable
     */
    public Optional<TaskSequence> getActionDefinition(Long toState) {
        Optional<StateTransition> transition = getTransitionFunctionByActivatedStateId(toState);
        if (transition.isEmpty() || !(transition.get() instanceof TaskSequenceStateTransition))
            return Optional.empty();
        return transition.get().getTaskSequenceOptional();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (stateId != null) stringBuilder.append("id=").append(stateId).append(", ");
        if (stateName != null) stringBuilder.append("from=").append(stateName).append(" ");
        stringBuilder.append("to=");
        transitions.forEach(
                transition -> {
                    if (transition instanceof TaskSequenceStateTransition) {
                        stringBuilder.append(transition.getActivate());
                    } else {
                        stringBuilder.append(((JavaStateTransition) transition).getActivateNames());
                        stringBuilder.append(",");
                    }
                });
        return stringBuilder.toString();
    }

    /**
     * Builder for creating StateTransitions instances fluently.
     *
     * <p>Simplifies construction of StateTransitions with multiple transitions and configuration
     * options. Provides sensible defaults:
     *
     * <ul>
     *   <li>Empty transition finish (always succeeds)
     *   <li>Empty transition list
     *   <li>State does not stay visible by default
     * </ul>
     *
     * <p>Example usage:
     *
     * <pre>
     * StateTransitions transitions = new StateTransitions.Builder("HomePage")
     *     .addTransitionFinish(() -> find("HomePageLogo"))
     *     .addTransition(() -> click("LoginButton"), "LoginPage")
     *     .addTransition(() -> click("SettingsIcon"), "SettingsPage")
     *     .build();
     * </pre>
     */
    public static class Builder {

        private final String stateName;
        private StateTransition transitionFinish = new JavaStateTransition.Builder().build();
        private final List<StateTransition> transitions = new ArrayList<>();
        private boolean staysVisibleAfterTransition = false;

        /**
         * Creates a builder for the specified state.
         *
         * @param stateName Name of the state these transitions belong to
         */
        public Builder(String stateName) {
            this.stateName = stateName;
        }

        /**
         * Adds a simple function-based outgoing transition.
         *
         * @param transition Function containing transition logic
         * @param toStates Names of states to activate on success
         * @return This builder for method chaining
         */
        public Builder addTransition(BooleanSupplier transition, String... toStates) {
            JavaStateTransition javaStateTransition =
                    new JavaStateTransition.Builder()
                            .setFunction(transition)
                            .addToActivate(toStates)
                            .build();
            return addTransition(javaStateTransition);
        }

        /**
         * Adds a pre-configured JavaStateTransition.
         *
         * @param transition Configured transition to add
         * @return This builder for method chaining
         */
        public Builder addTransition(JavaStateTransition transition) {
            transitions.add(transition);
            return this;
        }

        /**
         * Sets the transition finish function.
         *
         * <p>This function is executed when arriving at this state to verify and finalize the
         * transition.
         *
         * @param transitionFinish Function to verify state arrival
         * @return This builder for method chaining
         */
        public Builder addTransitionFinish(BooleanSupplier transitionFinish) {
            this.transitionFinish =
                    new JavaStateTransition.Builder().setFunction(transitionFinish).build();
            return this;
        }

        /**
         * Sets a pre-configured transition finish.
         *
         * @param transitionFinish Configured transition finish
         * @return This builder for method chaining
         */
        public Builder addTransitionFinish(JavaStateTransition transitionFinish) {
            this.transitionFinish = transitionFinish;
            return this;
        }

        /**
         * Sets default visibility behavior for outgoing transitions.
         *
         * <p>Individual transitions can override this default.
         *
         * @param staysVisibleAfterTransition true if state stays visible by default
         * @return This builder for method chaining
         */
        public Builder setStaysVisibleAfterTransition(boolean staysVisibleAfterTransition) {
            this.staysVisibleAfterTransition = staysVisibleAfterTransition;
            return this;
        }

        /**
         * Creates the StateTransitions with configured properties.
         *
         * @return Configured StateTransitions instance
         */
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
