package io.github.jspinak.brobot.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Placeholder for Brobot TransitionStep annotation.
 *
 * <p>This is a simplified placeholder for testing purposes. Marks a method as a step in a state transition.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TransitionStep {
    /**
     * The execution order of this step in the transition sequence.
     *
     * @return step order
     */
    int order() default 0;
}
