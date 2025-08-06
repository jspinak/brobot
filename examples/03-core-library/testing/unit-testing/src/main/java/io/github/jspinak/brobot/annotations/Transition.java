package io.github.jspinak.brobot.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Placeholder for Brobot Transition annotation.
 * In a real project, this would come from the Brobot library.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Transition {
    Class<?> from();
    Class<?> to();
    int priority() default 0;
}