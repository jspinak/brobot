package io.github.jspinak.brobot.debug;

import static org.junit.jupiter.api.Assumptions.assumeFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Base class for debug/diagnostic tests that require actual display. These tests are automatically
 * skipped in headless/mock environments.
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public abstract class DebugTestBase extends BrobotTestBase {

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();

        // Skip these tests in mock mode as they are for debugging real display issues
        assumeFalse(
                FrameworkSettings.mock,
                "Skipping debug/diagnostic test in mock mode - requires real display");

        // Additional check for headless environment
        assumeFalse(
                java.awt.GraphicsEnvironment.isHeadless(),
                "Skipping debug/diagnostic test in headless environment");
    }

    /** Helper method to check if we're in a WSL environment */
    protected boolean isWSL() {
        String wslDistro = System.getenv("WSL_DISTRO_NAME");
        return wslDistro != null && !wslDistro.isEmpty();
    }

    /** Helper method to check if we're in a CI environment */
    protected boolean isCI() {
        return "true".equals(System.getenv("CI"))
                || "true".equals(System.getenv("GITHUB_ACTIONS"))
                || "true".equals(System.getenv("JENKINS"));
    }
}
