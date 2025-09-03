package io.github.jspinak.brobot.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInfo;

import io.github.jspinak.brobot.config.mock.MockModeManager;

/**
 * Base test class for all Brobot tests.
 * Provides common setup and configuration for consistent test execution.
 * 
 * <p>This base class provides a foundation for testing:
 * <ul>
 *   <li>Common test setup and configuration</li>
 *   <li>Ensures tests work in Docker, CI/CD pipelines, and headless servers</li>
 *   <li>Provides utility methods for test scenarios</li>
 *   <li>Ensures proper mock mode initialization using MockModeManager</li>
 * </ul>
 */
public abstract class BrobotTestBase {
    
    /**
     * Global setup for all tests in the class.
     * Sets system properties to ensure headless operation and enables mock mode.
     */
    @BeforeAll
    public static void setUpBrobotEnvironment() {
        // Set test profile to use test-specific configuration
        System.setProperty("spring.profiles.active", "test");
        
        // Set test mode FIRST to prevent any blocking initialization
        System.setProperty("brobot.test.mode", "true");
        System.setProperty("brobot.test.type", "unit");
        
        // Disable blocking @PostConstruct operations during tests
        System.setProperty("brobot.diagnostics.image-loading.enabled", "false");
        System.setProperty("brobot.logging.capture.enabled", "false");
        System.setProperty("brobot.startup.verification.enabled", "false");
        
        // Disable all startup delays
        System.setProperty("brobot.startup.delay", "0");
        System.setProperty("brobot.startup.initial.delay", "0");
        System.setProperty("brobot.startup.ui.stabilization.delay", "0");
        
        // Ensure headless mode for all tests
        System.setProperty("java.awt.headless", "true");
        System.setProperty("sikuli.Debug", "0");
        
        // Disable SikuliX splash screen and popups
        System.setProperty("sikuli.console", "false");
        System.setProperty("sikuli.splashscreen", "false");
        
        // Set mock timings for fast test execution
        System.setProperty("brobot.mock.time.find.first", "0.01");
        System.setProperty("brobot.mock.time.find.all", "0.04");
        System.setProperty("brobot.mock.time.click", "0.01");
        System.setProperty("brobot.mock.time.type", "0.02");
        System.setProperty("brobot.mock.time.move", "0.01");
        System.setProperty("brobot.mock.time.drag", "0.03");
        System.setProperty("brobot.mock.time.vanish", "0.05");
        System.setProperty("brobot.mock.time.wait", "0.01");
        
        // Enable mock mode using MockModeManager for proper synchronization
        MockModeManager.setMockMode(true);
    }
    
    /**
     * Setup method that runs before each test.
     * Configures test environment to ensure tests work in all environments.
     * Subclasses should override and call super.setupTest() if they need additional setup.
     */
    @BeforeEach
    public void setupTest() {
        // Enable mock mode by default for testing using MockModeManager
        MockModeManager.setMockMode(true);
        
        // Reset any static state that might interfere between tests
        resetStaticState();
    }
    
    /**
     * Hook for subclasses to reset any static state between tests.
     * Override this method if your tests use static fields or singletons.
     */
    protected void resetStaticState() {
        // Subclasses can override to reset static state
    }
    
    /**
     * Utility method to temporarily disable mock mode for specific test scenarios.
     * Remember to re-enable it in a finally block or @AfterEach method.
     */
    protected void disableMockMode() {
        MockModeManager.setMockMode(false);
    }
    
    /**
     * Re-enables mock mode after it has been disabled.
     */
    protected void enableMockMode() {
        MockModeManager.setMockMode(true);
    }
    
    /**
     * Checks if mock mode is currently enabled.
     * 
     * @return true if mock mode is enabled, false otherwise
     */
    protected boolean isMockMode() {
        return MockModeManager.isMockMode();
    }
    
    /**
     * Utility method to log test execution for debugging.
     * 
     * @param testInfo Information about the current test
     */
    protected void logTestExecution(TestInfo testInfo) {
        System.out.printf("Running test: %s.%s%n", 
            testInfo.getTestClass().map(Class::getSimpleName).orElse("Unknown"),
            testInfo.getTestMethod().map(m -> m.getName()).orElse("unknown"));
    }
}