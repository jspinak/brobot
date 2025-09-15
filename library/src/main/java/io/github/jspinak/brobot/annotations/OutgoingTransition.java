package io.github.jspinak.brobot.annotations;

import java.lang.annotation.*;

/**
 * Marks a method as an OutgoingTransition - a transition FROM the state defined in the
 * enclosing @TransitionSet class TO a specific target state.
 *
 * <p>This annotation represents the cleaner pattern where a state's transition class contains its
 * incoming transition (ToTransition) and all its outgoing transitions. This is more cohesive since
 * outgoing transitions use the current state's images.
 *
 * <p>The annotated method should:
 *
 * <ul>
 *   <li>Return boolean (true if transition succeeds, false otherwise)
 *   <li>Contain the actions needed to navigate TO the target state
 *   <li>Be a member of a class annotated with @TransitionSet
 *   <li>Use images from the current state (defined in @TransitionSet)
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @OutgoingTransition(to = PricingState.class, priority = 1)
 * public boolean toPricing() {
 *     log.info("Navigating from Homepage to Pricing");
 *     return action.click(homepageState.getPricingLink()).isSuccess();
 * }
 * }</pre>
 *
 * @since 1.3.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OutgoingTransition {

    /**
     * The target state class for this transition. This transition will navigate FROM the state
     * defined in @TransitionSet TO this target state.
     *
     * @return the target state class
     */
    Class<?> to();

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
