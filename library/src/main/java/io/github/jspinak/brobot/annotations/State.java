package io.github.jspinak.brobot.annotations;

import java.lang.annotation.*;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * Annotation for Brobot states. This annotation marks a class as a Brobot state and
 * includes @Component for Spring component scanning.
 *
 * <p>Classes annotated with @State should also include: - @Getter from Lombok for generating
 * getters - @Slf4j from Lombok for logging
 *
 * <p><b>Hidden States:</b> When a state overlays another (e.g., a modal dialog opening over a
 * page), the framework automatically tracks the covered state as "hidden". This enables dynamic
 * transitions using {@code PreviousState.class} to return to whatever was covered. States don't
 * need to explicitly define their hidden states - this is managed automatically by the framework
 * based on state activation patterns.
 *
 * <p>Usage:
 *
 * <pre>
 * @State
 * @Getter
 * @Slf4j
 * public class PromptState {
 *     private StateObject submitButton = new StateObject.Builder()
 *         .withImage("submit")
 *         .build();
 * }
 * </pre>
 *
 * To mark as initial state:
 *
 * <pre>
 * @State(initial = true)
 * @Getter
 * @Slf4j
 * public class InitialState {
 *     // state definition
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@DependsOn("imageLoadingInitializer")
public @interface State {
    /**
     * Indicates whether this state is an initial state. Initial states are automatically registered
     * with the StateTransitionsJointTable as starting points for the state machine.
     *
     * @return true if this is an initial state, false otherwise
     */
    boolean initial() default false;

    /**
     * Optional name for the state. If not specified, the simple class name (without "State" suffix
     * if present) will be used.
     *
     * @return the state name
     */
    String name() default "";

    /**
     * Optional description of the state's purpose. This can be used for documentation and
     * debugging.
     *
     * @return the state description
     */
    String description() default "";

    /**
     * Priority for initial state selection (higher values = higher priority). Used when multiple
     * initial states are defined to influence selection probability. Default is 100 for equal
     * probability among all initial states. Only applies when initial = true.
     *
     * @return priority value for this initial state
     * @since 1.2.0
     */
    int priority() default 100;

    /**
     * Spring profiles where this state should be considered initial. Empty array means the state is
     * initial in all profiles. Only applies when initial = true.
     *
     * <p>Example: @State(initial = true, profiles = {"test", "development"})
     *
     * @return array of profile names where this state is initial
     * @since 1.2.0
     */
    String[] profiles() default {};

    /**
     * Path-finding cost for reaching this state. The total cost of a path is the sum of all state
     * costs and transition costs in that path. Lower costs are preferred when multiple paths exist.
     * Default is 1.
     *
     * <p>Example uses:
     *
     * <ul>
     *   <li>0 - Free state (no cost to be in this state)
     *   <li>1 - Normal state (default)
     *   <li>5 - Slightly expensive state (e.g., requires loading)
     *   <li>10+ - Expensive state to reach (e.g., error recovery states)
     * </ul>
     *
     * @return the path cost for being in this state
     * @since 1.3.0
     */
    int pathCost() default 1;
}
