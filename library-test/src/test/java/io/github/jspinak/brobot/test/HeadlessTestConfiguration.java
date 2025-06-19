package io.github.jspinak.brobot.test;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.BrobotEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import jakarta.annotation.PostConstruct;

/**
 * Test configuration for running tests in headless environments.
 * This configuration uses the new BrobotEnvironment to properly separate
 * mock mode from headless mode, allowing integration tests to work with
 * real images even in CI/CD environments.
 */
@TestConfiguration
public class HeadlessTestConfiguration {
    
    @PostConstruct
    public void setupHeadlessEnvironment() {
        // Configure BrobotEnvironment based on test type
        String testType = System.getProperty("brobot.test.type", "unit");
        
        BrobotEnvironment env;
        
        if ("integration".equals(testType)) {
            // Integration tests: use real files but no screen capture
            env = BrobotEnvironment.builder()
                .mockMode(false)  // Use real files
                .forceHeadless(true)  // But no screen capture
                .allowScreenCapture(false)
                .verboseLogging(true)
                .build();
            
            // Keep BrobotSettings.mock false for integration tests
            BrobotSettings.mock = false;
        } else {
            // Unit tests: full mock mode
            env = BrobotEnvironment.builder()
                .mockMode(true)  // Use fake data
                .build();
            
            // Keep BrobotSettings.mock true for backward compatibility
            BrobotSettings.mock = true;
        }
        
        BrobotEnvironment.setInstance(env);
        
        // Set headless property
        System.setProperty("java.awt.headless", "true");
        
        // Set default screen dimensions for tests
        System.setProperty("SCREEN_WIDTH", "1920");
        System.setProperty("SCREEN_HEIGHT", "1080");
        
        // Log the configuration
        System.out.println("Test environment configured: " + env.getEnvironmentInfo());
    }
}