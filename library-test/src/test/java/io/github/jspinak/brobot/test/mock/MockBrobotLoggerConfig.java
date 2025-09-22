package io.github.jspinak.brobot.test.mock;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import io.github.jspinak.brobot.config.logging.LoggingVerbosityConfig;
import io.github.jspinak.brobot.logging.BrobotLogger;
import io.github.jspinak.brobot.test.logging.TestLoggerFactory;

/**
 * Test configuration that provides a functional BrobotLogger for tests.
 *
 * <p>This configuration follows Single Responsibility Principle: - Uses TestLoggerFactory to handle
 * complex logger creation - Each bean method has a single responsibility of exposing a component -
 * No circular dependencies or @Lazy annotations needed
 */
@TestConfiguration
public class MockBrobotLoggerConfig {

    private TestLoggerFactory.LoggingSystem loggingSystem;

    /**
     * Initialize the logging system once using the factory. This ensures proper initialization
     * order without circular dependencies.
     */
    @Bean
    public TestLoggerFactory.LoggingSystem testLoggingSystem(
            LoggingVerbosityConfig verbosityConfig) {
        TestLoggerFactory factory = new TestLoggerFactory();
        // Pass null for actionLogger since it's been removed
        this.loggingSystem = factory.createTestLoggingSystem(null, verbosityConfig);
        return loggingSystem;
    }

    @Bean
    @Primary
    public BrobotLogger brobotLogger(TestLoggerFactory.LoggingSystem system) {
        return system.getLogger();
    }
}
