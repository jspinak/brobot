package io.github.jspinak.brobot.test;

import io.github.jspinak.brobot.config.ExecutionEnvironment;
import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.test.config.TestConfigurationManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import io.github.jspinak.brobot.test.config.TestActionConfig;

/**
 * Base class for Brobot integration tests with Spring context.
 * 
 * Architectural improvements:
 * - Uses TestConfigurationManager for early environment setup
 * - No circular dependencies or @Lazy annotations
 * - Clear separation of concerns
 * - Follows Single Responsibility Principle
 * 
 * Each component has a single responsibility:
 * - TestConfigurationManager: Initialize environment before Spring
 * - TestActionConfig: Provide action framework beans
 * - This base class: Manage test lifecycle and state
 */
@SpringBootTest(classes = io.github.jspinak.brobot.BrobotTestApplication.class)
@ContextConfiguration(initializers = TestConfigurationManager.class)
@Import(TestActionConfig.class)
@TestPropertySource(properties = {
    "spring.main.allow-bean-definition-overriding=true"
})
public abstract class BrobotIntegrationTestBase {
    
    private ExecutionEnvironment originalEnvironment;
    private boolean originalMockState;
    
    @BeforeEach
    protected void setUpBrobotEnvironment() {
        // Save original state for restoration
        originalEnvironment = ExecutionEnvironment.getInstance();
        originalMockState = FrameworkSettings.mock;
        
        // Environment is already configured by TestConfigurationManager
        // This method now has a single responsibility: save state for cleanup
        
        // Allow subclasses to add custom setup
        additionalSetup();
    }
    
    @AfterEach
    protected void tearDownBrobotEnvironment() {
        // Allow subclasses to clean up
        additionalTearDown();
        
        // Restore original state
        // Single responsibility: restore original environment
        if (originalEnvironment != null) {
            ExecutionEnvironment.setInstance(originalEnvironment);
        }
        FrameworkSettings.mock = originalMockState;
    }
    
    /**
     * Check if test environment is headless.
     * Single responsibility: query environment state.
     */
    protected boolean isHeadlessEnvironment() {
        return java.awt.GraphicsEnvironment.isHeadless();
    }
    
    /**
     * Check if screen capture is available.
     * Single responsibility: query screen capture capability.
     */
    protected boolean canCaptureScreen() {
        return ExecutionEnvironment.getInstance().canCaptureScreen();
    }
    
    /**
     * Check if using real files (not mocked).
     * Single responsibility: query file processing mode.
     */
    protected boolean useRealFiles() {
        return ExecutionEnvironment.getInstance().useRealFiles();
    }
    
    /**
     * Hook for subclasses to add setup logic.
     * Single responsibility: provide extension point for setup.
     */
    protected void additionalSetup() {
        // Default: no additional setup
    }
    
    /**
     * Hook for subclasses to add teardown logic.
     * Single responsibility: provide extension point for cleanup.
     */
    protected void additionalTearDown() {
        // Default: no additional teardown
    }
}