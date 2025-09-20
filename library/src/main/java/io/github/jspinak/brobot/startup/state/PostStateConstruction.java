package io.github.jspinak.brobot.startup.state;

import java.lang.annotation.*;

/**
 * Marks a method in a State class that should be called after the Spring context is fully
 * initialized to load images and complete state initialization.
 *
 * <p>This annotation enables deferred initialization of State images, preventing failures during
 * bean construction when the image loading infrastructure isn't ready yet.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @State
 * public class MyState {
 *     private StateImage button;
 *
 *     public MyState() {
 *         // Don't load images in constructor
 *     }
 *
 *     @PostStateConstruction
 *     public void loadImages() {
 *         // Load images here after context is ready
 *         button = new StateImage.Builder()
 *             .addPatterns("path/to/image")
 *             .build();
 *     }
 * }
 * }</pre>
 *
 * @since 1.1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PostStateConstruction {
    /** Optional description of what this initialization method does. */
    String value() default "";
}
