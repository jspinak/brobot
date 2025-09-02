package io.github.jspinak.brobot.test.categories;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a test as a unit test.
 * 
 * Unit tests should:
 * - Test a single class or method in isolation
 * - Use mocks for dependencies
 * - Run quickly (< 100ms)
 * - Not require external resources (files, network, display)
 * - Be deterministic and repeatable
 * 
 * Example usage:
 * <pre>
 * {@code
 * @UnitTest
 * class MyServiceTest {
 *     @Test
 *     void testBusinessLogic() {
 *         // Test implementation
 *     }
 * }
 * }
 * </pre>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("unit")
public @interface UnitTest {
}