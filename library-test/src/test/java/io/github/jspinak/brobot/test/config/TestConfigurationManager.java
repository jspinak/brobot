package io.github.jspinak.brobot.test.config;

import io.github.jspinak.brobot.config.ExecutionEnvironment;
import io.github.jspinak.brobot.config.FrameworkSettings;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Manages test configuration initialization with proper ordering.
 * 
 * Single Responsibility: Initialize test environment before Spring context loads.
 * This ensures ExecutionEnvironment and other static configurations are set
 * before any beans are created, preventing initialization conflicts.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TestConfigurationManager implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        configureTestEnvironment();
    }
    
    /**
     * Configure the test environment before Spring beans are created.
     * This method has a single responsibility: set up the test environment.
     */
    private void configureTestEnvironment() {
        // Prevent ExecutionEnvironment from overriding headless settings
        System.setProperty("brobot.preserve.headless.setting", "true");
        System.setProperty("java.awt.headless", "true");
        
        // Set screen dimensions for consistent testing
        System.setProperty("SCREEN_WIDTH", "1920");
        System.setProperty("SCREEN_HEIGHT", "1080");
        
        // Determine test type
        String testType = System.getProperty("brobot.test.type", "unit");
        boolean isIntegrationTest = "integration".equals(testType);
        
        // Configure ExecutionEnvironment based on test type
        ExecutionEnvironment env = ExecutionEnvironment.builder()
            .mockMode(!isIntegrationTest)  // Mock for unit tests, real for integration
            .forceHeadless(true)  // Always headless in tests
            .allowScreenCapture(false)  // No screen capture in tests
            .verboseLogging(System.getProperty("brobot.test.verbose", "false").equals("true"))
            .build();
        
        ExecutionEnvironment.setInstance(env);
        
        // Set FrameworkSettings for backward compatibility
        FrameworkSettings.mock = !isIntegrationTest;
        
        // Log configuration
        if (env.isVerboseLogging()) {
            System.out.println("Test environment initialized: " + env.getEnvironmentInfo());
            System.out.println("Test type: " + testType);
            System.out.println("Mock mode: " + env.isMockMode());
        }
    }
}