package io.github.jspinak.brobot.test.mock;

import io.github.jspinak.brobot.config.LoggingVerbosityConfig;
import io.github.jspinak.brobot.logging.unified.*;
import io.github.jspinak.brobot.logging.unified.console.ConsoleFormatter;
import io.github.jspinak.brobot.tools.logging.ActionLogger;
import io.github.jspinak.brobot.tools.logging.ConsoleReporterInitializer;
import io.github.jspinak.brobot.tools.logging.spi.LogSink;
import io.github.jspinak.brobot.tools.logging.spi.NoOpLogSink;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import jakarta.annotation.PostConstruct;

/**
 * Test configuration that provides a functional BrobotLogger for tests.
 * This replaces the mock with a real implementation that works in test environments.
 */
@TestConfiguration
public class MockBrobotLoggerConfig {
    
    @Bean
    @Primary
    public LoggingContext loggingContext() {
        return new LoggingContext();
    }
    
    @Bean
    @Primary  
    public LogSink logSink() {
        // Use NoOpLogSink for tests to avoid file I/O
        return new NoOpLogSink();
    }
    
    @Bean
    @Primary
    public ConsoleFormatter consoleFormatter(LoggingVerbosityConfig verbosityConfig) {
        return new ConsoleFormatter(verbosityConfig);
    }
    
    @Bean
    @Primary
    public MessageRouter messageRouter(ActionLogger actionLogger, 
                                      LoggingVerbosityConfig verbosityConfig,
                                      ConsoleFormatter consoleFormatter) {
        return new MessageRouter(actionLogger, verbosityConfig, consoleFormatter);
    }
    
    @Bean
    @Primary
    public BrobotLogger brobotLogger(LoggingContext context, MessageRouter router) {
        return new BrobotLogger(context, router);
    }
    
    @Bean
    @Primary
    public ConsoleReporterInitializer consoleReporterInitializer(BrobotLogger brobotLogger) {
        return new ConsoleReporterInitializer(brobotLogger);
    }
    
    /**
     * Ensure ConsoleReporter is initialized even if the ConsoleReporterInitializer
     * PostConstruct doesn't run in tests.
     */
    @PostConstruct
    public void initConsoleReporter() {
        // This will be called after all beans are created
        // We'll let ConsoleReporterInitializer handle this
    }
}