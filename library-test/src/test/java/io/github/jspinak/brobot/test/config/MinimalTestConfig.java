package io.github.jspinak.brobot.test.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import io.github.jspinak.brobot.logging.unified.ConsoleOutputCapture;
import io.github.jspinak.brobot.startup.BrobotStartup;
import io.github.jspinak.brobot.startup.AutoStartupVerifier;
import io.github.jspinak.brobot.config.*;
import io.github.jspinak.brobot.diagnostics.ImageLoadingDiagnosticsRunner;
import io.github.jspinak.brobot.annotations.AnnotationProcessor;

/**
 * Minimal test configuration that excludes components that might block during initialization.
 * This configuration is specifically designed to prevent test hanging issues.
 */
@TestConfiguration
@ComponentScan(
    basePackages = "io.github.jspinak.brobot",
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = {
                ConsoleOutputCapture.class,
                BrobotStartup.class,
                AutoStartupVerifier.class,
                PhysicalResolutionCapture.class,
                HeadlessDiagnostics.class,
                ImageLoadingDiagnosticsRunner.class,
                FrameworkInitializer.class,
                WindowsAutoScaleConfig.class,
                BrobotDPIConfig.class,
                BrobotDPIConfiguration.class,
                AutoScalingConfiguration.class,
                AnnotationProcessor.class
            }
        ),
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = ".*\\$.*" // Exclude inner classes
        )
    }
)
public class MinimalTestConfig {
    
    static {
        // Ensure headless mode
        System.setProperty("java.awt.headless", "true");
        
        // Disable console capture
        System.setProperty("brobot.console.capture.enabled", "false");
        
        // Enable mock mode
        System.setProperty("brobot.mock.enabled", "true");
        System.setProperty("brobot.framework.mock", "true");
        
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
    
    /**
     * Mock ConsoleOutputCapture to prevent blocking
     */
    @Bean
    @Primary
    public ConsoleOutputCapture mockConsoleOutputCapture() {
        return new ConsoleOutputCapture() {
            @Override
            public void startCapture() {
                // Do nothing - prevent blocking
            }
            
            @Override
            public void stopCapture() {
                // Do nothing
            }
        };
    }
}