package io.github.jspinak.brobot.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.config.mock.MockProperties;
import io.github.jspinak.brobot.tools.logging.console.ConsoleActionConfig;
import io.github.jspinak.brobot.tools.logging.gui.GuiAccessConfig;
import io.github.jspinak.brobot.tools.logging.visual.VisualFeedbackConfig;

/**
 * Tests that configuration classes no longer have Java defaults. When instantiated directly
 * (without Spring), fields should be null/default primitive values.
 */
class PropertiesLoadingTest {

    @Test
    void testGuiAccessConfigNoJavaDefaults() {
        GuiAccessConfig config = new GuiAccessConfig();

        // All fields should be uninitialized (false for booleans, 0 for ints)
        assertFalse(
                config.isContinueOnError(),
                "continueOnError should be false without properties file");
        assertFalse(
                config.isReportProblems(),
                "reportProblems should be false without properties file");
        assertEquals(
                0,
                config.getMinScreenWidth(),
                "minScreenWidth should be 0 without properties file");
        assertEquals(
                0,
                config.getMinScreenHeight(),
                "minScreenHeight should be 0 without properties file");
    }

    @Test
    void testConsoleActionConfigNoJavaDefaults() {
        ConsoleActionConfig config = new ConsoleActionConfig();

        // All fields should be uninitialized
        assertFalse(config.isEnabled(), "enabled should be false without properties file");
        assertNull(config.getLevel(), "level should be null without properties file");
        assertEquals(
                0,
                config.getPerformanceWarnThreshold(),
                "performanceWarnThreshold should be 0 without properties file");
        assertNull(config.getIndentPrefix(), "indentPrefix should be null without properties file");
    }

    @Test
    void testVisualFeedbackConfigNoJavaDefaults() {
        VisualFeedbackConfig config = new VisualFeedbackConfig();

        // Main config fields should be uninitialized
        assertFalse(config.isEnabled(), "enabled should be false without properties file");
        assertFalse(
                config.isAutoHighlightFinds(),
                "autoHighlightFinds should be false without properties file");

        // Nested configs should still be instantiated (for Spring to populate them)
        assertNotNull(config.getFind(), "Find config should be instantiated");
        assertNotNull(config.getSearchRegion(), "SearchRegion config should be instantiated");
        assertNotNull(config.getError(), "Error config should be instantiated");
        assertNotNull(config.getClick(), "Click config should be instantiated");

        // But their fields should have no defaults
        assertNull(
                config.getFind().getColor(), "Find color should be null without properties file");
        assertEquals(
                0.0,
                config.getFind().getDuration(),
                0.001,
                "Find duration should be 0.0 without properties file");
    }

    @Test
    void testMockPropertiesNoJavaDefaults() {
        MockProperties config = new MockProperties();

        // Fields should be uninitialized
        assertFalse(config.isEnabled(), "enabled should be false without properties file");

        // Nested config should be instantiated
        assertNotNull(config.getAction(), "Action config should be instantiated");
        assertEquals(
                0.0,
                config.getAction().getSuccessProbability(),
                0.001,
                "successProbability should be 0.0 without properties file");
    }
}
