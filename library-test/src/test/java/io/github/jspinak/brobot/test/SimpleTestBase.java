package io.github.jspinak.brobot.test;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import io.github.jspinak.brobot.config.core.BrobotProperties;

/**
 * Simple base test class for tests that don't require Spring context. Sets up mock mode to ensure
 * tests work in headless environments.
 */
public abstract class SimpleTestBase {

    @Autowired private BrobotProperties brobotProperties;

    @BeforeEach
    public void setupMockMode() {
        // Set system property for mock mode
        System.setProperty("brobot.mock.mode", "true");

        // Set brobotProperties.getCore().isMock() directly using reflection to avoid initialization
        // issues
        try {
            Class<?> frameworkSettingsClass =
                    Class.forName("io.github.jspinak.brobot.config.core.BrobotProperties");
            frameworkSettingsClass.getField("mock").set(null, true);
        } catch (Exception e) {
            // Ignore if BrobotProperties is not available
        }
    }
}
