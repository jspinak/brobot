package io.github.jspinak.brobot.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Annotation for Brobot states.
 * This annotation marks a class as a Brobot state and includes @Component
 * for Spring component scanning.
 * 
 * Classes annotated with @State should also include:
 * - @Getter from Lombok for generating getters
 * - @Slf4j from Lombok for logging
 * 
 * Usage:
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
public @interface State {
    /**
     * Indicates whether this state is an initial state.
     * Initial states are automatically registered with the StateTransitionsJointTable
     * as starting points for the state machine.
     * 
     * @return true if this is an initial state, false otherwise
     */
    boolean initial() default false;
    
    /**
     * Optional name for the state. If not specified, the simple class name
     * (without "State" suffix if present) will be used.
     * 
     * @return the state name
     */
    String name() default "";
    
    /**
     * Optional description of the state's purpose.
     * This can be used for documentation and debugging.
     * 
     * @return the state description
     */
    String description() default "";
    
    /**
     * Priority for initial state selection (higher values = higher priority).
     * Used when multiple initial states are defined to influence selection probability.
     * Default is 100 for equal probability among all initial states.
     * Only applies when initial = true.
     * 
     * @return priority value for this initial state
     * @since 1.2.0
     */
    int priority() default 100;
    
    /**
     * Spring profiles where this state should be considered initial.
     * Empty array means the state is initial in all profiles.
     * Only applies when initial = true.
     * 
     * Example: @State(initial = true, profiles = {"test", "development"})
     * 
     * @return array of profile names where this state is initial
     * @since 1.2.0
     */
    String[] profiles() default {};
}