package io.github.jspinak.brobot.tools.logging;

import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

/**
 * Initializes ConsoleReporter with the BrobotLogger instance during Spring startup.
 * This ensures that all ConsoleReporter calls are routed through the unified logging system.
 */
@Component
public class ConsoleReporterInitializer {
    
    private final BrobotLogger brobotLogger;
    
    @Autowired
    public ConsoleReporterInitializer(BrobotLogger brobotLogger) {
        this.brobotLogger = brobotLogger;
    }
    
    @PostConstruct
    public void init() {
        ConsoleReporter.setBrobotLogger(brobotLogger);
    }
}