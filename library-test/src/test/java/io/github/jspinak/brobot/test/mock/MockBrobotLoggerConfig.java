package io.github.jspinak.brobot.test.mock;

import io.github.jspinak.brobot.config.LoggingVerbosityConfig;
import io.github.jspinak.brobot.logging.unified.*;
import io.github.jspinak.brobot.logging.unified.console.ConsoleFormatter;
import io.github.jspinak.brobot.test.logging.TestLoggerFactory;
import io.github.jspinak.brobot.tools.logging.ActionLogger;
import io.github.jspinak.brobot.tools.logging.ConsoleReporterInitializer;
import io.github.jspinak.brobot.tools.logging.spi.LogSink;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Test configuration that provides a functional BrobotLogger for tests.
 * 
 * This configuration follows Single Responsibility Principle:
 * - Uses TestLoggerFactory to handle complex logger creation
 * - Each bean method has a single responsibility of exposing a component
 * - No circular dependencies or @Lazy annotations needed
 */
@TestConfiguration
public class MockBrobotLoggerConfig {
    
    private TestLoggerFactory.LoggingSystem loggingSystem;
    
    /**
     * Initialize the logging system once using the factory.
     * This ensures proper initialization order without circular dependencies.
     */
    @Bean
    public TestLoggerFactory.LoggingSystem testLoggingSystem(
            ActionLogger actionLogger,
            LoggingVerbosityConfig verbosityConfig) {
        TestLoggerFactory factory = new TestLoggerFactory();
        this.loggingSystem = factory.createTestLoggingSystem(actionLogger, verbosityConfig);
        return loggingSystem;
    }
    
    @Bean
    @Primary
    public LoggingContext loggingContext(TestLoggerFactory.LoggingSystem system) {
        return system.getContext();
    }
    
    @Bean
    @Primary  
    public LogSink logSink(TestLoggerFactory.LoggingSystem system) {
        return system.getLogSink();
    }
    
    @Bean
    @Primary
    public ConsoleFormatter consoleFormatter(TestLoggerFactory.LoggingSystem system) {
        return system.getFormatter();
    }
    
    @Bean
    @Primary
    public MessageRouter messageRouter(TestLoggerFactory.LoggingSystem system) {
        return system.getRouter();
    }
    
    @Bean
    @Primary
    public BrobotLogger brobotLogger(TestLoggerFactory.LoggingSystem system) {
        return system.getLogger();
    }
    
    @Bean
    @Primary
    public ConsoleReporterInitializer consoleReporterInitializer(TestLoggerFactory.LoggingSystem system) {
        return system.getReporterInit();
    }
}