package io.github.jspinak.brobot.test;

import org.junit.jupiter.api.BeforeEach;

/**
 * Simple base test class for tests that don't require Spring context.
 * Sets up mock mode to ensure tests work in headless environments.
 */
public abstract class SimpleTestBase {

    @BeforeEach
    public void setupMockMode() {
        // Set system property for mock mode
        System.setProperty("brobot.mock.mode", "true");

        // Set FrameworkSettings.mock directly using reflection to avoid initialization
        // issues
        try {
            Class<?> frameworkSettingsClass = Class.forName("io.github.jspinak.brobot.config.core.FrameworkSettings");
            frameworkSettingsClass.getField("mock").set(null, true);
        } catch (Exception e) {
            // Ignore if FrameworkSettings is not available
        }
    }
}