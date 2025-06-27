package io.github.jspinak.brobot.navigation.transition;

import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.runner.dsl.model.TaskSequence;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;

/**
 * Code-based state transition implementation for the Brobot framework.
 * 
 * <p>JavaStateTransition represents transitions defined through Java code using BooleanSupplier 
 * functions. This implementation enables dynamic, programmatic state navigation where transition 
 * logic can involve complex conditions, external data, or runtime calculations that cannot be 
 * expressed declaratively.</p>
 * 
 * <p>Key components:
 * <ul>
 *   <li><b>Transition Function</b>: BooleanSupplier that executes the transition logic</li>
 *   <li><b>Activation List</b>: States to activate after successful transition</li>
 *   <li><b>Exit List</b>: States to deactivate after successful transition</li>
 *   <li><b>Visibility Control</b>: Whether source state remains visible post-transition</li>
 *   <li><b>Path Score</b>: Weight for path-finding algorithms (higher = less preferred)</li>
 * </ul>
 * </p>
 * 
 * <p>State reference handling:
 * <ul>
 *   <li>Uses state names during definition (IDs not yet assigned)</li>
 *   <li>Names are converted to IDs during initialization</li>
 *   <li>Both name and ID sets are maintained for flexibility</li>
 *   <li>Supports multiple target states for branching transitions</li>
 * </ul>
 * </p>
 * 
 * <p>Transition execution flow:
 * <ol>
 *   <li>BooleanSupplier is invoked to perform transition logic</li>
 *   <li>If true is returned, transition is considered successful</li>
 *   <li>States in 'activate' set become active</li>
 *   <li>States in 'exit' set become inactive</li>
 *   <li>Success counter is incremented for metrics</li>
 * </ol>
 * </p>
 * 
 * <p>Common use patterns:
 * <ul>
 *   <li>Complex navigation logic that depends on runtime conditions</li>
 *   <li>Transitions involving external API calls or data validation</li>
 *   <li>Dynamic state activation based on application state</li>
 *   <li>Fallback transitions with custom error handling</li>
 * </ul>
 * </p>
 * 
 * <p>Builder pattern benefits:
 * <ul>
 *   <li>Fluent API for readable transition definitions</li>
 *   <li>Default values for optional properties</li>
 *   <li>Type-safe state name specification</li>
 *   <li>Convenient overloads for common patterns</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, JavaStateTransition provides the flexibility needed for 
 * complex automation scenarios where declarative ActionDefinitions are insufficient. It 
 * enables seamless integration of custom logic while maintaining the benefits of the 
 * state graph structure for navigation and path finding.</p>
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
    private Set<String> exitNames = new HashSet<>();;
    private Set<Long> activate = new HashSet<>();;
    private Set<Long> exit = new HashSet<>();;
    private int score = 0;
    private int timesSuccessful = 0;

    @Override
    public Optional<TaskSequence> getTaskSequenceOptional() {
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "activate=" + activateNames + ", exit=" + exitNames + '}';
    }

    /**
     * Builder for creating JavaStateTransition instances fluently.
     * <p>
     * Provides a readable API for constructing transitions with sensible defaults:
     * <ul>
     *   <li>Default function returns false (transition fails)</li>
     *   <li>Default visibility is NONE (inherit from transition type)</li>
     *   <li>Empty activation and exit sets by default</li>
     *   <li>Zero score (highest priority)</li>
     * </ul>
     * <p>
     * Example usage:
     * <pre>
     * JavaStateTransition transition = new JavaStateTransition.Builder()
     *     .setFunction(() -> action.click("Next"))
     *     .addToActivate("PageTwo")
     *     .addToExit("PageOne")
     *     .setScore(10)
     *     .build();
     * </pre>
     */
    public static class Builder {
        private BooleanSupplier transitionFunction = () -> false;
        private StaysVisible staysVisibleAfterTransition = StaysVisible.NONE;
        private Set<String> activate = new HashSet<>();
        private Set<String> exit = new HashSet<>();
        private int score = 0;

        /**
         * Sets the transition logic function.
         * <p>
         * The BooleanSupplier should contain all logic needed to execute
         * the transition, returning true on success and false on failure.
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
         * <p>
         * Controls whether the source state remains visible after transition:
         * <ul>
         *   <li>TRUE: Source state stays active</li>
         *   <li>FALSE: Source state is deactivated</li>
         *   <li>NONE: Behavior determined by transition type</li>
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
         * <p>
         * Simplified method for common TRUE/FALSE cases.
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
         * <p>
         * These states will become active when the transition succeeds.
         * Can be called multiple times to accumulate states.
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
         * <p>
         * These states will be deactivated when the transition succeeds.
         * Can be called multiple times to accumulate states.
         *
         * @param stateNames Variable number of state names to exit
         * @return This builder for method chaining
         */
        public Builder addToExit(String... stateNames) {
            this.exit.addAll(List.of(stateNames));
            return this;
        }

        /**
         * Sets the path-finding score for this transition.
         * <p>
         * Higher scores make this transition less preferred when multiple
         * paths exist. Score 0 is highest priority.
         *
         * @param score Path-finding weight (0 = best)
         * @return This builder for method chaining
         */
        public Builder setScore(int score) {
            this.score = score;
            return this;
        }

        /**
         * Creates the JavaStateTransition with configured properties.
         * <p>
         * Constructs a new instance with all builder settings applied.
         * The builder can be reused after calling build().
         *
         * @return Configured JavaStateTransition instance
         */
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
