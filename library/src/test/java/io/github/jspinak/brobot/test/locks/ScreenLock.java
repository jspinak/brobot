package io.github.jspinak.brobot.test.locks;

import org.junit.jupiter.api.parallel.ResourceLock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a test requires exclusive access to screen capture functionality.
 * Tests with this annotation will not run in parallel with other screen-locked tests.
 * 
 * Use this for:
 * - Tests that capture screenshots
 * - Tests that use SikuliX Screen class
 * - Tests that interact with display/monitor functionality
 * - Tests that perform visual pattern matching
 * 
 * Example:
 * <pre>
 * {@code
 * @Test
 * @ScreenLock
 * void testScreenCapture() {
 *     // This test has exclusive screen access
 * }
 * }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ResourceLock("SCREEN")
public @interface ScreenLock {
}