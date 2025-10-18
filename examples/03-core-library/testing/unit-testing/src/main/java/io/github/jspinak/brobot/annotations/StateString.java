package io.github.jspinak.brobot.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Placeholder for Brobot StateString annotation.
 *
 * <p>This is a simplified placeholder for testing purposes. Marks a field as a state string
 * (text that appears in or affects this state).
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StateString {
    /**
     * The name of the state string.
     *
     * @return string name
     */
    String name() default "";
}
