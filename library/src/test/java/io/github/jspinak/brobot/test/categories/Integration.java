package io.github.jspinak.brobot.test.categories;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Marks a test as an integration test.
 *
 * <p>Integration tests should: - Test interaction between multiple components - Use Spring context
 * if needed - May use real implementations instead of mocks - Can access files and databases - Run
 * in seconds (< 5s typically)
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @Integration
 * @SpringBootTest
 * class ServiceIntegrationTest {
 *     @Autowired
 *     private MyService service;
 *
 *     @Test
 *     void testServiceIntegration() {
 *         // Test implementation
 *     }
 * }
 * }</pre>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("integration")
public @interface Integration {}
