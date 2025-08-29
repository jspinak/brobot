package io.github.jspinak.brobot.tools.logging;

import io.github.jspinak.brobot.config.LoggingVerbosityConfig;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

/**
 * Initializes ConsoleReporter with the BrobotLogger instance during Spring startup.
 * This ensures that all ConsoleReporter calls are routed through the unified logging system.
 * 
 * Also configures the ConsoleReporter output level based on the configured verbosity
 * to prevent excessive debug logging from internal components like SearchRegionResolver.
 */
@Component
public class ConsoleReporterInitializer {
    
    private final BrobotLogger brobotLogger;
    private final LoggingVerbosityConfig loggingVerbosityConfig;
    
    @Autowired
    public ConsoleReporterInitializer(BrobotLogger brobotLogger, LoggingVerbosityConfig loggingVerbosityConfig) {
        this.brobotLogger = brobotLogger;
        this.loggingVerbosityConfig = loggingVerbosityConfig;
    }
    
    @PostConstruct
    public void init() {
        // Set the BrobotLogger instance
        ConsoleReporter.setBrobotLogger(brobotLogger);
        
        // Configure output level based on verbosity
        configureOutputLevel();
    }
    
    /**
     * Maps LoggingVerbosityConfig levels to ConsoleReporter output levels.
     * This provides consistent logging behavior across the framework.
     */
    private void configureOutputLevel() {
        switch (loggingVerbosityConfig.getVerbosity()) {
            case QUIET:
                ConsoleReporter.outputLevel = ConsoleReporter.OutputLevel.NONE;
                break;
            case NORMAL:
                ConsoleReporter.outputLevel = ConsoleReporter.OutputLevel.LOW;
                break;
            case VERBOSE:
                ConsoleReporter.outputLevel = ConsoleReporter.OutputLevel.HIGH;
                break;
            default:
                ConsoleReporter.outputLevel = ConsoleReporter.OutputLevel.LOW;
        }
        
        // Log the configuration
        System.out.println("ConsoleReporter output level set to: " + ConsoleReporter.outputLevel);
    }
}