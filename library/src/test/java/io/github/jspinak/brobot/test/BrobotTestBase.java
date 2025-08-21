package io.github.jspinak.brobot.test;

import io.github.jspinak.brobot.config.ExecutionEnvironment;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base test class for all Brobot tests.
 * Provides common setup and configuration for consistent test execution.
 * 
 * <p>This base class automatically configures Brobot for testing:
 * <ul>
 *   <li>Enables mock mode for headless/CI environments</li>
 *   <li>Configures fast mock timings (0.01-0.04s for operations)</li>
 *   <li>Prevents AWTException and HeadlessException errors</li>
 *   <li>Ensures tests work in Docker, CI/CD pipelines, and headless servers</li>
 * </ul>
 */
public abstract class BrobotTestBase {
    
    /**
     * Setup method that runs before each test.
     * Configures mock mode to ensure tests work in all environments.
     * Subclasses should override and call super.setupTest() if they need additional setup.
     */
    @BeforeEach
    public void setupTest() {
        // Set system property for mock mode
        System.setProperty("brobot.mock.mode", "true");
        
        // Configure ExecutionEnvironment for mock mode
        ExecutionEnvironment env = new ExecutionEnvironment.Builder()
            .mockMode(true)
            .forceHeadless(true)
            .allowScreenCapture(false)
            .build();
        ExecutionEnvironment.setInstance(env);
        
        // Set FrameworkSettings.mock directly using reflection to avoid initialization issues
        try {
            Class<?> frameworkSettingsClass = Class.forName("io.github.jspinak.brobot.config.FrameworkSettings");
            frameworkSettingsClass.getField("mock").set(null, true);
        } catch (Exception e) {
            // Ignore if FrameworkSettings is not available
        }
    }
}