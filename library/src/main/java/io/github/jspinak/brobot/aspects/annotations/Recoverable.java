package io.github.jspinak.brobot.aspects.annotations;

import java.lang.annotation.*;

/**
 * Marks a method for automatic error recovery with retry logic.
 *
 * <p>Methods annotated with @Recoverable will be automatically retried by the ErrorRecoveryAspect
 * when they throw exceptions. The retry behavior can be customized using the annotation parameters.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @Recoverable(maxRetries = 3, delay = 1000, backoff = 2.0)
 * public ActionResult performCriticalAction() {
 *     // Action that might fail transiently
 * }
 *
 * @Recoverable(retryOn = {NetworkException.class}, skipOn = {ValidationException.class})
 * public void networkOperation() {
 *     // Only retry on network errors
 * }
 * }</pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Recoverable {

    /** Maximum number of retry attempts. Default is 3 attempts. */
    int maxRetries() default 3;

    /** Initial delay between retries in milliseconds. Default is 1000ms (1 second). */
    long delay() default 1000;

    /**
     * Exponential backoff multiplier. Each retry delay will be multiplied by this factor. Default
     * is 2.0 (doubling delay each time).
     */
    double backoff() default 2.0;

    /**
     * Maximum total time to spend retrying in milliseconds. Default is 30000ms (30 seconds). Set to
     * -1 for no timeout.
     */
    long timeout() default 30000;

    /** Exception types that should trigger a retry. Empty array means retry on any exception. */
    Class<? extends Throwable>[] retryOn() default {};

    /** Exception types that should NOT trigger a retry. Takes precedence over retryOn. */
    Class<? extends Throwable>[] skipOn() default {};

    /**
     * Whether to use jitter to randomize retry delays. Helps prevent thundering herd problems.
     * Default is true.
     */
    boolean jitter() default true;

    /** Recovery strategy to use. */
    RecoveryStrategy strategy() default RecoveryStrategy.EXPONENTIAL_BACKOFF;

    /**
     * Fallback method name to call if all retries fail. Method must have the same parameters as the
     * annotated method. Empty string means no fallback.
     */
    String fallbackMethod() default "";

    /** Whether to log retry attempts. Default is true. */
    boolean logRetries() default true;

    /** Recovery strategies */
    enum RecoveryStrategy {
        /** Fixed delay between retries */
        FIXED_DELAY,

        /** Exponentially increasing delay */
        EXPONENTIAL_BACKOFF,

        /** Fibonacci sequence delays */
        FIBONACCI_BACKOFF,

        /** Custom strategy (requires fallbackMethod) */
        CUSTOM
    }
}
