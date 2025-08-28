package io.github.jspinak.brobot.test.config;

import org.springframework.boot.test.context.TestConfiguration;

/**
 * Test configuration for state navigation components.
 * Provides mock implementations that support testing without real state management.
 * 
 * Note: StateService and StateTransitions require complex dependencies,
 * so they should be mocked at the test level using @Mock annotations.
 */
@TestConfiguration
public class StateNavigationTestConfig {
    // Configuration is handled at test level with @Mock annotations
    // This class remains as a placeholder for future configuration needs
}