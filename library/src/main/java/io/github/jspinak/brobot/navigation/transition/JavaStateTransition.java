package io.github.jspinak.brobot.navigation.transition;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;

import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.runner.dsl.model.TaskSequence;

import lombok.Getter;
import lombok.Setter;

/**
 * Code-based state transition implementation for the Brobot framework.
 *
 * <p>JavaStateTransition represents transitions defined through Java code using BooleanSupplier
 * functions. This implementation enables dynamic, programmatic state navigation where transition
 * logic can involve complex conditions, external data, or runtime calculations that cannot be
 * expressed declaratively.
 *
 * <p>Key components:
 *
 * <ul>
 *   <li><b>Transition Function</b>: BooleanSupplier that executes the transition logic
 *   <li><b>Activation List</b>: States to activate after successful transition
 *   <li><b>Exit List</b>: States to deactivate after successful transition
 *   <li><b>Visibility Control</b>: Whether source state remains visible post-transition
 *   <li><b>Path Cost</b>: Weight for path-finding algorithms (higher = less preferred)
 * </ul>
 *
 * <p>State reference handling:
 *
 * <ul>
 *   <li>Uses state names during definition (IDs not yet assigned)
 *   <li>Names are converted to IDs during initialization
 *   <li>Both name and ID sets are maintained for flexibility
 *   <li>Supports multiple target states for branching transitions
 * </ul>
 *
 * <p>Transition execution flow:
 *
 * <ol>
 *   <li>BooleanSupplier is invoked to perform transition logic
 *   <li>If true is returned, transition is considered successful
 *   <li>States in 'activate' set become active
 *   <li>States in 'exit' set become inactive
 *   <li>Success counter is incremented for metrics
 * </ol>
 *
 * <p>Common use patterns:
 *
 * <ul>
 *   <li>Complex navigation logic that depends on runtime conditions
 *   <li>Transitions involving external API calls or data validation
 *   <li>Dynamic state activation based on application state
 *   <li>Fallback transitions with custom error handling
 * </ul>
 *
 * <p>Builder pattern benefits:
 *
 * <ul>
 *   <li>Fluent API for readable transition definitions
 *   <li>Default values for optional properties
 *   <li>Type-safe state name specification
 *   <li>Convenient overloads for common patterns
 * </ul>
 *
 * <p>In the model-based approach, JavaStateTransition provides the flexibility needed for complex
 * automation scenarios where declarative ActionDefinitions are insufficient. It enables seamless
 * integration of custom logic while maintaining the benefits of the state graph structure for
 * navigation and path finding.
 *
 * @since 1.0
 * @see StateTransition
 * @see TaskSequenceStateTransition
 * @see StateTransitions
 * @see BooleanSupplier
 */
@Getter
@Setter
public class JavaStateTransition implements StateTransition {
    private String type = "java";
    private BooleanSupplier transitionFunction;

    private StaysVisible staysVisibleAfterTransition;
    /*
    Use names instead of IDs for coding these by hand since state ids are set at runtime.
     */
    private Set<String> activateNames = new HashSet<>();
    private Set<String> exitNames = new HashSet<>();
    ;
    private Set<Long> activate = new HashSet<>();
    ;
    private Set<Long> exit = new HashSet<>();
    ;
    private int pathCost = 1; // Default cost is 1 for normal transitions
    private int timesSuccessful = 0;

    @Override
    public Optional<TaskSequence> getTaskSequenceOptional() {
        return Optional.empty();
    }

    // Implement new interface methods
    @Override
    public int getPathCost() {
        return pathCost;
    }

    @Override
    public void setPathCost(int pathCost) {
        this.pathCost = pathCost;
    }

    @Override
    public String toString() {
        return "activate=" + activateNames + ", exit=" + exitNames + '}';
    }

    /**
     * Builder for creating JavaStateTransition instances fluently.
     *
     * <p>Provides a readable API for constructing transitions with sensible defaults:
     *
     * <ul>
     *   <li>Default function returns false (transition fails)
     *   <li>Default visibility is NONE (inherit from transition type)
     *   <li>Empty activation and exit sets by default
     *   <li>Zero path cost (highest priority)
     * </ul>
     *
     * <p>Example usage:
     *
     * <pre>
     * JavaStateTransition transition = new JavaStateTransition.Builder()
     *     .setFunction(() -> action.click("Next"))
     *     .addToActivate("PageTwo")
     *     .addToExit("PageOne")
     *     .setPathCost(10)
     *     .build();
     * </pre>
     */
    public static class Builder {
        private BooleanSupplier transitionFunction = () -> false;
        private StaysVisible staysVisibleAfterTransition = StaysVisible.NONE;
        private Set<String> activate = new HashSet<>();
        private Set<String> exit = new HashSet<>();
        private int pathCost = 1; // Default cost is 1 for normal transitions

        /**
         * Sets the transition logic function.
         *
         * <p>The BooleanSupplier should contain all logic needed to execute the transition,
         * returning true on success and false on failure.
         *
         * @param booleanSupplier Function containing transition logic
         * @return This builder for method chaining
         */
        public Builder setFunction(BooleanSupplier booleanSupplier) {
            this.transitionFunction = booleanSupplier;
            return this;
        }

        /**
         * Sets visibility behavior using StaysVisible enum.
         *
         * <p>Controls whether the source state remains visible after transition:
         *
         * <ul>
         *   <li>TRUE: Source state stays active
         *   <li>FALSE: Source state is deactivated
         *   <li>NONE: Behavior determined by transition type
         * </ul>
         *
         * @param staysVisibleAfterTransition Visibility behavior
         * @return This builder for method chaining
         */
        public Builder setStaysVisibleAfterTransition(StaysVisible staysVisibleAfterTransition) {
            this.staysVisibleAfterTransition = staysVisibleAfterTransition;
            return this;
        }

        /**
         * Sets visibility behavior using boolean convenience method.
         *
         * <p>Simplified method for common TRUE/FALSE cases.
         *
         * @param staysVisibleAfterTransition true if source stays visible
         * @return This builder for method chaining
         */
        public Builder setStaysVisibleAfterTransition(boolean staysVisibleAfterTransition) {
            if (staysVisibleAfterTransition) this.staysVisibleAfterTransition = StaysVisible.TRUE;
            else this.staysVisibleAfterTransition = StaysVisible.FALSE;
            return this;
        }

        /**
         * Adds states to activate after successful transition.
         *
         * <p>These states will become active when the transition succeeds. Can be called multiple
         * times to accumulate states.
         *
         * @param stateNames Variable number of state names to activate
         * @return This builder for method chaining
         */
        public Builder addToActivate(String... stateNames) {
            this.activate.addAll(List.of(stateNames));
            return this;
        }

        /**
         * Adds states to exit after successful transition.
         *
         * <p>These states will be deactivated when the transition succeeds. Can be called multiple
         * times to accumulate states.
         *
         * @param stateNames Variable number of state names to exit
         * @return This builder for method chaining
         */
        public Builder addToExit(String... stateNames) {
            this.exit.addAll(List.of(stateNames));
            return this;
        }

        /**
         * Sets the path-finding cost for this transition.
         *
         * <p>Higher costs make this transition less preferred when multiple paths exist. Cost 0 is
         * most preferred.
         *
         * @param pathCost Path-finding weight (0 = best)
         * @return This builder for method chaining
         */
        public Builder setPathCost(int pathCost) {
            this.pathCost = pathCost;
            return this;
        }

        /**
         * Creates the JavaStateTransition with configured properties.
         *
         * <p>Constructs a new instance with all builder settings applied. The builder can be reused
         * after calling build().
         *
         * @return Configured JavaStateTransition instance
         */
        public JavaStateTransition build() {
            JavaStateTransition javaStateTransition = new JavaStateTransition();
            javaStateTransition.transitionFunction = transitionFunction;
            javaStateTransition.staysVisibleAfterTransition = staysVisibleAfterTransition;
            javaStateTransition.activateNames = activate;
            javaStateTransition.exitNames = exit;
            javaStateTransition.pathCost = pathCost;
            return javaStateTransition;
        }
    }
}
