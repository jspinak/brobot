package io.github.jspinak.brobot.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Placeholder for Brobot StateObject annotation.
 *
 * <p>This is a simplified placeholder for testing purposes. Marks a field as a state object
 * (typically an image filename or StateImage reference).
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StateObject {
    /**
     * The name of the state object.
     *
     * @return object name
     */
    String name() default "";
}
