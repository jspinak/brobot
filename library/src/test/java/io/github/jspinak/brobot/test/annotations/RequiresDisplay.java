package io.github.jspinak.brobot.test.annotations;

import java.lang.annotation.*;

import org.junit.jupiter.api.extension.ExtendWith;

import io.github.jspinak.brobot.test.extensions.DisplayRequirementExtension;

/**
 * Marks a test or test class as requiring a real display. Tests with this annotation will be
 * skipped in WSL, headless environments, or CI/CD.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * @RequiresDisplay
 * @Test
 * void testScreenCapture() {
 *     // Test that requires real screen capture
 * }
 * }</pre>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(DisplayRequirementExtension.class)
public @interface RequiresDisplay {
    /**
     * Optional reason for requiring display.
     *
     * @return explanation of why display is required
     */
    String value() default "Requires real display for screen capture";
}
