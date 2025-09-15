package io.github.jspinak.brobot.annotations;

import java.lang.annotation.*;

/**
 * Marks a method as an IncomingTransition (also known as arrival transition or verification
 * transition). This transition verifies that we have successfully arrived at the target state.
 *
 * <p>The annotated method should:
 *
 * <ul>
 *   <li>Return boolean (true if state is confirmed, false otherwise)
 *   <li>Verify the presence of unique elements that confirm the state is active
 *   <li>Be a member of a class annotated with @TransitionSet
 *   <li>There should be only ONE @IncomingTransition method per @TransitionSet class
 * </ul>
 *
 * <p>This transition is executed to confirm successful navigation to the target state, regardless
 * of which state we came from. It verifies the state is active and ready.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @IncomingTransition
 * public boolean verifyArrival() {
 *     log.info("Verifying arrival at Pricing state");
 *     return action.find(pricingState.getStartForFreeButton()).isSuccess();
 * }
 * }</pre>
 *
 * @since 1.3.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IncomingTransition {

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
