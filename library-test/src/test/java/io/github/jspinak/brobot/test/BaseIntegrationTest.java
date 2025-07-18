package io.github.jspinak.brobot.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import io.github.jspinak.brobot.config.ExecutionEnvironment;

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
        ExecutionEnvironment env = ExecutionEnvironment.builder()
            .mockMode(false)  // Use real files
            .fromEnvironment()  // Allow override from environment variables
            .verboseLogging(true)
            .build();
        
        ExecutionEnvironment.setInstance(env);
        
        System.out.println("Integration test environment: " + env.getEnvironmentInfo());
    }
    
    @BeforeEach
    void logTestStart() {
        ExecutionEnvironment env = ExecutionEnvironment.getInstance();
        System.out.println("Running test with environment: " + env.getEnvironmentInfo());
    }
    
    /**
     * Helper method for tests that need to temporarily switch to mock mode.
     * Remember to restore the original environment after the test.
     * 
     * @param mockMode true to enable mock mode
     * @return the previous environment for restoration
     */
    protected ExecutionEnvironment switchToMockMode(boolean mockMode) {
        ExecutionEnvironment current = ExecutionEnvironment.getInstance();
        ExecutionEnvironment newEnv = ExecutionEnvironment.builder()
            .mockMode(mockMode)
            .forceHeadless(current.hasDisplay() ? false : true)
            .allowScreenCapture(current.canCaptureScreen())
            .build();
        ExecutionEnvironment.setInstance(newEnv);
        return current;
    }
    
    /**
     * Restore a previously saved environment configuration.
     * 
     * @param env the environment to restore
     */
    protected void restoreEnvironment(ExecutionEnvironment env) {
        ExecutionEnvironment.setInstance(env);
    }
}