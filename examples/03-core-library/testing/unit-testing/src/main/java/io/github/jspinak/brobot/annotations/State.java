package io.github.jspinak.brobot.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Placeholder for Brobot State annotation.
 *
 * <p>This is a simplified placeholder for testing purposes. In a real Brobot application,
 * this annotation comes from the Brobot library and includes Spring component scanning.
 *
 * <p>For this unit testing example, we use a minimal version that doesn't trigger
 * Brobot's annotation processing, allowing us to focus on testing patterns rather
 * than full framework integration.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface State {
    /**
     * The name of the state.
     *
     * @return state name
     */
    String name() default "";
}
