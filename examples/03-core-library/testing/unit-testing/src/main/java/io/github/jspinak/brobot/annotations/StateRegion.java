package io.github.jspinak.brobot.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Placeholder for Brobot StateRegion annotation.
 *
 * <p>This is a simplified placeholder for testing purposes. Marks a field as a state region
 * (a clickable or hoverable area in this state).
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StateRegion {
    /**
     * The name of the state region.
     *
     * @return region name
     */
    String name() default "";
}
