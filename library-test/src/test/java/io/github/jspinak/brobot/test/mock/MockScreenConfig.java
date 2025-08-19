package io.github.jspinak.brobot.test.mock;

import org.springframework.boot.test.context.TestConfiguration;

/**
 * Test configuration for Screen in headless test environment.
 * Disables Screen bean creation for tests.
 */
@TestConfiguration
public class MockScreenConfig {
    
    // We don't create any Screen bean at all for tests
    // This prevents SikuliX from trying to initialize
}