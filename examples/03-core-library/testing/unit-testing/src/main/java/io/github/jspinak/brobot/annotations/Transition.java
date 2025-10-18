package io.github.jspinak.brobot.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Placeholder for Brobot Transition annotation.
 *
 * <p>This is a simplified placeholder for testing purposes. Marks a class as a state transition.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Transition {
    /**
     * The source state for this transition.
     *
     * @return source state class
     */
    Class<?> from() default Object.class;

    /**
     * The destination state for this transition.
     *
     * @return destination state class
     */
    Class<?> to() default Object.class;
}
