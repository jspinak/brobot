package io.github.jspinak.brobot.annotations;

import java.lang.annotation.*;

/**
 * Marks a method as a FromTransition - a transition FROM a specific state TO the state defined in
 * the enclosing @TransitionSet class.
 *
 * <p>The annotated method should:
 *
 * <ul>
 *   <li>Return boolean (true if transition succeeds, false otherwise)
 *   <li>Contain the actions needed to navigate FROM the source state
 *   <li>Be a member of a class annotated with @TransitionSet
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @FromTransition(from = MenuState.class, priority = 1)
 * public boolean fromMenu() {
 *     log.info("Navigating from Menu to Pricing");
 *     return action.click(menuState.getPricingButton()).isSuccess();
 * }
 * }</pre>
 *
 * @since 1.2.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FromTransition {

    /**
     * The source state class for this transition. This transition will navigate FROM this state TO
     * the state defined in @TransitionSet.
     *
     * @return the source state class
     */
    Class<?> from();

    /**
     * Priority of this transition when multiple paths exist. Higher values indicate higher
     * priority. Default is 0.
     *
     * @return the transition priority
     */
    int priority() default 0;

    /**
     * Optional description of this transition. Useful for documentation and debugging.
     *
     * @return the transition description
     */
    String description() default "";

    /**
     * Timeout for this transition in seconds. Default is 10 seconds.
     *
     * @return the timeout in seconds
     */
    int timeout() default 10;
}
