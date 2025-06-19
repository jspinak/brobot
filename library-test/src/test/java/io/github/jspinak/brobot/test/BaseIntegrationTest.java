package io.github.jspinak.brobot.test;

import io.github.jspinak.brobot.actions.BrobotEnvironment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * Base class for integration tests that need to work with real image files
 * but may run in headless environments (like CI/CD).
 * 
 * <p>This class configures BrobotEnvironment to:
 * <ul>
 *   <li>Use real files (not mock) for image loading and processing</li>
 *   <li>Skip screen capture operations in headless environments</li>
 *   <li>Provide clear logging of the environment configuration</li>
 * </ul>
 * 
 * <p>Integration tests should extend this class to ensure proper configuration.
 */
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestEnvironmentInitializer.class)
public abstract class BaseIntegrationTest {
    
    @BeforeAll
    static void setupIntegrationTestEnvironment() {
        // Set system property to indicate integration test
        System.setProperty("brobot.test.type", "integration");
        
        // Configure environment for integration tests
        BrobotEnvironment env = BrobotEnvironment.builder()
            .mockMode(false)  // Use real files
            .fromEnvironment()  // Allow override from environment variables
            .verboseLogging(true)
            .build();
        
        BrobotEnvironment.setInstance(env);
        
        System.out.println("Integration test environment: " + env.getEnvironmentInfo());
    }
    
    @BeforeEach
    void logTestStart() {
        BrobotEnvironment env = BrobotEnvironment.getInstance();
        System.out.println("Running test with environment: " + env.getEnvironmentInfo());
    }
    
    /**
     * Helper method for tests that need to temporarily switch to mock mode.
     * Remember to restore the original environment after the test.
     * 
     * @param mockMode true to enable mock mode
     * @return the previous environment for restoration
     */
    protected BrobotEnvironment switchToMockMode(boolean mockMode) {
        BrobotEnvironment current = BrobotEnvironment.getInstance();
        BrobotEnvironment newEnv = BrobotEnvironment.builder()
            .mockMode(mockMode)
            .forceHeadless(current.hasDisplay() ? false : true)
            .allowScreenCapture(current.canCaptureScreen())
            .build();
        BrobotEnvironment.setInstance(newEnv);
        return current;
    }
    
    /**
     * Restore a previously saved environment configuration.
     * 
     * @param env the environment to restore
     */
    protected void restoreEnvironment(BrobotEnvironment env) {
        BrobotEnvironment.setInstance(env);
    }
}