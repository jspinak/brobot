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
 * page), use the canHide parameter to declare which states can be hidden. This enables automatic
 * hidden state tracking and dynamic transitions using {@code PreviousState.class} to return to
 * whatever was covered.
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
 *
 * For modal states that can hide other states:
 *
 * <pre>
 * @State(
 *     description = "Modal dialog overlay",
 *     canHide = {"MainPage", "SettingsPage"}
 * )
 * @Getter
 * @Slf4j
 * public class ModalDialogState {
 *     // PreviousState transitions will automatically return to hidden state
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
     * @since 1.1.0
     */
    int priority() default 100;

    /**
     * Spring profiles where this state should be considered initial. Empty array means the state is
     * initial in all profiles. Only applies when initial = true.
     *
     * <p>Example: @State(initial = true, profiles = {"test", "development"})
     *
     * @return array of profile names where this state is initial
     * @since 1.1.0
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
     * @since 1.1.0
     */
    int pathCost() default 1;

    /**
     * Names of states that this state can hide when it becomes active.
     *
     * <p>This is CRITICAL for PreviousState transitions to work correctly. When a modal or overlay
     * state becomes active, Brobot needs to know which states it can potentially cover. By
     * specifying canHide, you enable automatic hidden state tracking.
     *
     * <p><b>Example use case:</b> A modal dialog that can appear over either MainPage or
     * SettingsPage should declare: {@code @State(canHide = {"MainPage", "SettingsPage"})}
     *
     * <p>When the modal closes and has a PreviousState transition, Brobot will automatically
     * return to whichever state was hidden.
     *
     * <p><b>Important:</b> The state names in canHide must match the actual state names (either
     * derived from class name or explicitly set via the name parameter).
     *
     * @return array of state names that this state can hide
     * @since 1.1.0
     */
    String[] canHide() default {};
}
