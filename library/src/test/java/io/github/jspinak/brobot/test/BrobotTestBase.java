package io.github.jspinak.brobot.test;

import io.github.jspinak.brobot.config.MockModeManager;
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
        // Use the centralized MockModeManager for consistency
        MockModeManager.setMockMode(true);
    }
    
    /**
     * Utility method to temporarily disable mock mode for specific test scenarios.
     * Remember to re-enable it in a finally block or @AfterEach method.
     */
    protected void disableMockMode() {
        MockModeManager.setMockMode(false);
    }
    
    /**
     * Checks if mock mode is currently enabled.
     * 
     * @return true if mock mode is enabled, false otherwise
     */
    protected boolean isMockMode() {
        return MockModeManager.isMockMode();
    }
}