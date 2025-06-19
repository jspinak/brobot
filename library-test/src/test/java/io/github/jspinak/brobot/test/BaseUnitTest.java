package io.github.jspinak.brobot.test;

import io.github.jspinak.brobot.actions.BrobotEnvironment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.ContextConfiguration;

/**
 * Base class for unit tests that use mock data and don't require real files
 * or screen access.
 * 
 * <p>This class configures BrobotEnvironment to:
 * <ul>
 *   <li>Use mock mode for all operations</li>
 *   <li>Return fake data instead of accessing files or screen</li>
 *   <li>Run quickly without external dependencies</li>
 * </ul>
 * 
 * <p>Unit tests should extend this class when they don't need real image
 * processing or file access.
 */
@ContextConfiguration(initializers = TestEnvironmentInitializer.class)
public abstract class BaseUnitTest {
    
    @BeforeAll
    static void setupUnitTestEnvironment() {
        // Set system property to indicate unit test
        System.setProperty("brobot.test.type", "unit");
        
        // Configure environment for unit tests
        BrobotEnvironment env = BrobotEnvironment.builder()
            .mockMode(true)  // Use mock data
            .forceHeadless(true)  // Always headless for unit tests
            .allowScreenCapture(false)
            .build();
        
        BrobotEnvironment.setInstance(env);
        
        System.out.println("Unit test environment: " + env.getEnvironmentInfo());
    }
    
    @BeforeEach
    void ensureMockMode() {
        // Ensure mock mode is still enabled (in case a test changed it)
        BrobotEnvironment env = BrobotEnvironment.getInstance();
        if (!env.isMockMode()) {
            System.out.println("WARNING: Mock mode was disabled, re-enabling for unit test");
            BrobotEnvironment mockEnv = BrobotEnvironment.builder()
                .mockMode(true)
                .forceHeadless(true)
                .allowScreenCapture(false)
                .build();
            BrobotEnvironment.setInstance(mockEnv);
        }
    }
}