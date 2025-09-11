package io.github.jspinak.brobot.test.locks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * Indicates that a test requires exclusive access to Spring context initialization. Tests with this
 * annotation will not run in parallel with other Spring-context-locked tests.
 *
 * <p>Use this for: - Tests that use @SpringBootTest - Tests that modify Spring context
 * configuration - Tests that use ApplicationContextRunner - Tests with @DirtiesContext
 *
 * <p>Note: This should primarily be used in the library-test module.
 *
 * <p>Example:
 *
 * <pre>{@code
 * @SpringBootTest
 * @SpringContextLock
 * class ApplicationIntegrationTest {
 *     // This test has exclusive Spring context access
 * }
 * }</pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ResourceLock("SPRING_CONTEXT")
public @interface SpringContextLock {}
