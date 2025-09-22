package io.github.jspinak.brobot.test.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import io.github.jspinak.brobot.annotations.AnnotationProcessor;
import io.github.jspinak.brobot.config.*;
import io.github.jspinak.brobot.diagnostics.ImageLoadingDiagnosticsRunner;
import io.github.jspinak.brobot.startup.orchestration.StartupRunner;
import io.github.jspinak.brobot.startup.verification.AutoStartupVerifier;

/**
 * Minimal test configuration that excludes components that might block during initialization. This
 * configuration is specifically designed to prevent test hanging issues.
 */
@TestConfiguration
@ComponentScan(
        basePackages = "io.github.jspinak.brobot",
        excludeFilters = {
            @ComponentScan.Filter(
                    type = FilterType.ASSIGNABLE_TYPE,
                    classes = {
                        StartupRunner.class,
                        AutoStartupVerifier.class,
                        ImageLoadingDiagnosticsRunner.class,
                        AnnotationProcessor.class
                    }),
            @ComponentScan.Filter(
                    type = FilterType.REGEX,
                    pattern = ".*\\$.*" // Exclude inner classes
                    )
        })
public class MinimalTestConfig {

    static {
        // Ensure headless mode
        System.setProperty("java.awt.headless", "true");

        // Disable console capture
        System.setProperty("brobot.console.capture.enabled", "false");

        // Enable mock mode
        System.setProperty("brobot.mock", "true");
        System.setProperty("brobot.mock", "true");

        // Disable screen capture
        System.setProperty("brobot.screen.capture.enabled", "false");

        // Disable GUI access checks
        System.setProperty("brobot.gui-access.check-on-startup", "false");

        // Set test timeouts
        System.setProperty("brobot.test.timeout", "5000");

        // Disable physical resolution capture
        System.setProperty("brobot.capture.physical-resolution", "false");

        // Disable diagnostics
        System.setProperty("brobot.diagnostics.enabled", "false");
    }

    // ConsoleOutputCapture has been removed from the library
    // This bean is no longer needed
}
