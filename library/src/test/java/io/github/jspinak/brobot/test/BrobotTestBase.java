package io.github.jspinak.brobot.test;

import io.github.jspinak.brobot.config.ExecutionEnvironment;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.util.ReflectionTestUtils;

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
        // Configure ExecutionEnvironment for testing
        ExecutionEnvironment testEnv = ExecutionEnvironment.builder()
            .mockMode(true)  // Enable mock mode for testing
            .forceHeadless(false)  // Allow display operations in mock mode
            .allowScreenCapture(false)  // No real screen capture in tests
            .build();
        
        ExecutionEnvironment.setInstance(testEnv);
        
        // Also set system property for compatibility
        System.setProperty("brobot.mock.mode", "true");
    }
}