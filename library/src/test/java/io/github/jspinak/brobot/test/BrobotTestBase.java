package io.github.jspinak.brobot.test;

import org.junit.jupiter.api.BeforeEach;

/**
 * Base test class for all Brobot tests.
 * Provides common setup and configuration for consistent test execution.
 * 
 * <p>This base class provides a foundation for testing:
 * <ul>
 *   <li>Common test setup and configuration</li>
 *   <li>Ensures tests work in Docker, CI/CD pipelines, and headless servers</li>
 *   <li>Provides utility methods for test scenarios</li>
 * </ul>
 */
public abstract class BrobotTestBase {
    
    private boolean mockMode = true;
    
    /**
     * Setup method that runs before each test.
     * Configures test environment to ensure tests work in all environments.
     * Subclasses should override and call super.setupTest() if they need additional setup.
     */
    @BeforeEach
    public void setupTest() {
        // Enable mock mode by default for testing
        mockMode = true;
    }
    
    /**
     * Utility method to temporarily disable mock mode for specific test scenarios.
     * Remember to re-enable it in a finally block or @AfterEach method.
     */
    protected void disableMockMode() {
        mockMode = false;
    }
    
    /**
     * Checks if mock mode is currently enabled.
     * 
     * @return true if mock mode is enabled, false otherwise
     */
    protected boolean isMockMode() {
        return mockMode;
    }
}