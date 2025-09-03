package io.github.jspinak.brobot.test.locks;

import org.junit.jupiter.api.parallel.ResourceLock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a test requires exclusive access to mouse control.
 * Tests with this annotation will not run in parallel with other mouse-locked tests.
 * 
 * Use this for:
 * - Tests that move or click the mouse
 * - Tests that track mouse position
 * - Tests that simulate user mouse interactions
 * - Tests using SikuliX Mouse class
 * 
 * Example:
 * <pre>
 * {@code
 * @Test
 * @MouseLock
 * void testMouseMovement() {
 *     // This test has exclusive mouse control
 * }
 * }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ResourceLock("MOUSE")
public @interface MouseLock {
}