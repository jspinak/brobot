package io.github.jspinak.brobot.test;

import org.junit.jupiter.api.BeforeEach;

/**
 * Base test class for all Brobot tests.
 * Provides common setup and configuration for consistent test execution.
 */
public abstract class BrobotTestBase {
    
    /**
     * Setup method that runs before each test.
     * Subclasses should override and call super.setupTest() if they need additional setup.
     */
    @BeforeEach
    public void setupTest() {
        // Common test setup can be added here
        // For now, this is a placeholder for future common setup logic
    }
}