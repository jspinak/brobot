package io.github.jspinak.brobot.test;

import io.github.jspinak.brobot.config.ExecutionEnvironment;
import io.github.jspinak.brobot.config.FrameworkSettings;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import io.github.jspinak.brobot.test.TestEnvironmentInitializer;
import io.github.jspinak.brobot.test.mock.MockGuiAccessConfig;
import io.github.jspinak.brobot.test.mock.MockGuiAccessMonitor;
import io.github.jspinak.brobot.test.mock.MockScreenConfig;

/**
 * Base class for Brobot integration tests that properly configures
 * the test environment to support real image processing in headless mode.
 * 
 * This base class ensures that:
 * - Integration tests can load and process real images from files
 * - Screen capture operations return appropriate dummy images
 * - Tests work consistently in both local and CI/CD environments
 * 
 * Key principle: Integration tests should use real image processing but
 * handle headless environments gracefully.
 */
@SpringBootTest(classes = io.github.jspinak.brobot.BrobotTestApplication.class)
@TestPropertySource(properties = {
    "spring.main.allow-bean-definition-overriding=true"
})
public abstract class BrobotIntegrationTestBase {
    
    private boolean originalMockState;
    private ExecutionEnvironment originalEnvironment;
    
    @BeforeEach
    protected void setUpBrobotEnvironment() {
        // Save original state
        originalMockState = FrameworkSettings.mock;
        originalEnvironment = ExecutionEnvironment.getInstance();
        
        // Configure environment for integration tests
        // Always use real files for integration tests, but handle headless gracefully
        ExecutionEnvironment env = ExecutionEnvironment.builder()
                .mockMode(false)  // Use real files and image processing
                .forceHeadless(isHeadlessEnvironment())  // Auto-detect or force headless
                .allowScreenCapture(false)  // No screen capture in tests
                .verboseLogging(false)  // Enable for debugging
                .build();
        
        ExecutionEnvironment.setInstance(env);
        FrameworkSettings.mock = false;  // Ensure legacy flag is also set correctly
        
        // Set AWT headless property
        System.setProperty("java.awt.headless", String.valueOf(isHeadlessEnvironment()));
        
        // Allow subclasses to add custom setup
        additionalSetup();
    }
    
    @AfterEach
    protected void tearDownBrobotEnvironment() {
        // Allow subclasses to clean up
        additionalTearDown();
        
        // Restore original state
        FrameworkSettings.mock = originalMockState;
        if (originalEnvironment != null) {
            ExecutionEnvironment.setInstance(originalEnvironment);
        }
    }
    
    /**
     * Determines if tests should run in headless mode.
     * Can be overridden by subclasses for specific test requirements.
     * 
     * @return true if headless mode should be used
     */
    protected boolean isHeadlessEnvironment() {
        // Check system property first
        String headlessProp = System.getProperty("java.awt.headless");
        if (headlessProp != null) {
            return Boolean.parseBoolean(headlessProp);
        }
        
        // Check for CI environment
        return System.getenv("CI") != null || 
               System.getenv("GITHUB_ACTIONS") != null ||
               System.getenv("JENKINS_URL") != null;
    }
    
    /**
     * Convenience method to check if the current test can capture screen.
     * Useful for conditional test logic.
     * 
     * @return true if screen capture is available
     */
    protected boolean canCaptureScreen() {
        return ExecutionEnvironment.getInstance().canCaptureScreen();
    }
    
    /**
     * Convenience method to check if real files are being used.
     * Should always return true for integration tests.
     * 
     * @return true if real file operations are enabled
     */
    protected boolean useRealFiles() {
        return ExecutionEnvironment.getInstance().useRealFiles();
    }
    
    /**
     * Override this method to add additional setup logic
     */
    protected void additionalSetup() {
        // Default: no additional setup
    }
    
    /**
     * Override this method to add additional teardown logic
     */
    protected void additionalTearDown() {
        // Default: no additional teardown
    }
}