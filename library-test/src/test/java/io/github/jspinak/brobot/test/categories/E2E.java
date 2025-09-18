package io.github.jspinak.brobot.test.categories;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Marks a test as an end-to-end test.
 *
 * <p>E2E tests should: - Test complete user workflows - May require display/GUI - Use real screen
 * capture and pattern matching - Test the system as a whole - Can be slow (minutes)
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @E2E
 * @DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Test incompatible with CI environment")
 * class ApplicationE2ETest {
 *     @Test
 *     void testCompleteUserWorkflow() {
 *         // Test implementation
 *     }
 * }
 * }</pre>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("e2e")
public @interface E2E {}
