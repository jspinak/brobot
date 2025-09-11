package com.example.logging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Demonstrates action logging best practices in Brobot.
 *
 * <p>This example shows: - Structured logging with SLF4J - Logging at appropriate levels -
 * Including context with MDC - Action chain logging - Performance tracking with logs
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.logging", "io.github.jspinak.brobot"})
public class ActionLoggingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActionLoggingApplication.class, args);
    }
}
