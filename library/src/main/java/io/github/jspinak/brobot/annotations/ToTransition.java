package io.github.jspinak.brobot.annotations;

import java.lang.annotation.*;

/**
 * Marks a method as a ToTransition (also known as arrival transition or finish transition). This
 * transition verifies that we have successfully arrived at the target state.
 *
 * <p>The annotated method should:
 *
 * <ul>
 *   <li>Return boolean (true if state is confirmed, false otherwise)
 *   <li>Verify the presence of unique elements that confirm the state is active
 *   <li>Be a member of a class annotated with @TransitionSet
 *   <li>There should be only ONE @ToTransition method per @TransitionSet class
 * </ul>
 *
 * <p>This transition is executed after any FromTransition to confirm successful navigation to the
 * target state, regardless of which state we came from.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @ToTransition
 * public boolean verifyArrival() {
 *     log.info("Verifying arrival at Pricing state");
 *     return action.find(pricingState.getStartForFreeButton()).isSuccess();
 * }
 * }</pre>
 *
 * @since 1.2.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ToTransition {

    /**
     * Optional description of this arrival verification. Useful for documentation and debugging.
     *
     * @return the transition description
     */
    String description() default "";

    /**
     * Timeout for verifying arrival in seconds. Default is 5 seconds.
     *
     * @return the timeout in seconds
     */
    int timeout() default 5;

    /**
     * Whether this verification is required for the transition to be considered successful. If
     * false, failure of this verification will log a warning but not fail the transition. Default
     * is true.
     *
     * @return whether this verification is required
     */
    boolean required() default true;
}
