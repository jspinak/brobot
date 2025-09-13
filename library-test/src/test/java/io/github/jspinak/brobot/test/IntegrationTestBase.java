package io.github.jspinak.brobot.test;

import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jspinak.brobot.config.mock.MockModeManager;

/** Base class for integration tests. Ensures mock mode is properly configured before each test. */
public abstract class IntegrationTestBase {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @BeforeEach
    public void setupTest() {
        // Ensure mock mode is enabled
        MockModeManager.setMockMode(true);
        System.setProperty("brobot.mock", "true");
        System.setProperty("brobot.mock", "true");
        System.setProperty("java.awt.headless", "true");

        log.debug("Integration test setup complete - mock mode enabled");
    }
}
