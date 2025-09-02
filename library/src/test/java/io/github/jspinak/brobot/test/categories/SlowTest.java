package io.github.jspinak.brobot.test.categories;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a test as slow (takes more than 1 second).
 * 
 * Slow tests:
 * - Can be excluded during rapid development cycles
 * - Should be run in CI/CD pipelines
 * - May involve file I/O, network calls, or complex computations
 * 
 * Can be combined with other categories:
 * <pre>
 * {@code
 * @IntegrationTest
 * @SlowTest
 * class DatabaseIntegrationTest {
 *     @Test
 *     void testLargeDatasetProcessing() {
 *         // Test implementation
 *     }
 * }
 * }
 * </pre>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("slow")
public @interface SlowTest {
}