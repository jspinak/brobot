package io.github.jspinak.brobot.test.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import io.github.jspinak.brobot.logging.BrobotLogger;
import io.github.jspinak.brobot.test.mock.LogCapture;
import io.github.jspinak.brobot.test.mock.MockLoggerFactory;

/**
 * Spring test configuration for logging components. Provides mock logger beans for integration
 * tests.
 */
@TestConfiguration
@Profile("test")
public class TestLoggingConfiguration {

    /**
     * Creates the mock logger factory.
     *
     * @return A MockLoggerFactory instance
     */
    @Bean
    public MockLoggerFactory mockLoggerFactory() {
        return new MockLoggerFactory();
    }

    /**
     * Creates the log capture utility.
     *
     * @return A LogCapture instance for test verification
     */
    @Bean
    public LogCapture logCapture() {
        return new LogCapture();
    }

    /**
     * Creates the primary test logger bean. This logger will be injected into components during
     * tests.
     *
     * @param factory The mock logger factory
     * @return A mock BrobotLogger
     */
    @Bean
    @Primary
    public BrobotLogger testLogger(MockLoggerFactory factory) {
        return factory.createMockLogger();
    }

    /**
     * Creates a capturing logger for integration tests. This logger records all log entries for
     * verification.
     *
     * @param logCapture The log capture utility
     * @return A capturing BrobotLogger
     */
    @Bean(name = "capturingLogger")
    public BrobotLogger capturingLogger(LogCapture logCapture) {
        return logCapture.createCapturingLogger();
    }

    /**
     * Creates a silent logger for performance tests. This logger minimizes overhead by doing
     * nothing.
     *
     * @param factory The mock logger factory
     * @return A silent BrobotLogger
     */
    @Bean(name = "silentLogger")
    public BrobotLogger silentLogger(MockLoggerFactory factory) {
        return factory.createSilentLogger();
    }
}
