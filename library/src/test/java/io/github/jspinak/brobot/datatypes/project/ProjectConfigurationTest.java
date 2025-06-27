package io.github.jspinak.brobot.datatypes.project;

import io.github.jspinak.brobot.runner.project.AutomationConfiguration;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ProjectConfiguration class.
 */
class ProjectConfigurationTest {

    @Test
    void testDefaultValues() {
        AutomationConfiguration config = new AutomationConfiguration();

        assertEquals(0.7, config.getMinSimilarity(), "Default minSimilarity should be 0.7");
        assertEquals(0.5, config.getMoveMouseDelay(), "Default moveMouseDelay should be 0.5");
        assertEquals(0.3, config.getDelayBeforeMouseDown(), "Default delayBeforeMouseDown should be 0.3");
        assertEquals(0.3, config.getDelayAfterMouseDown(), "Default delayAfterMouseDown should be 0.3");
        assertEquals(0.3, config.getDelayBeforeMouseUp(), "Default delayBeforeMouseUp should be 0.3");
        assertEquals(0.3, config.getDelayAfterMouseUp(), "Default delayAfterMouseUp should be 0.3");
        assertEquals(0.3, config.getTypeDelay(), "Default typeDelay should be 0.3");
        assertEquals(0.5, config.getPauseBetweenActions(), "Default pauseBetweenActions should be 0.5");
        assertEquals(10.0, config.getMaxWait(), "Default maxWait should be 10.0");
        assertEquals("INFO", config.getLogLevel(), "Default logLevel should be INFO");
        assertTrue(config.getIllustrationEnabled(), "Default illustrationEnabled should be true");
        assertNull(config.getAutomationFunctions(), "Default automationFunctions should be null");
    }

    @Test
    void testGettersAndSetters() {
        AutomationConfiguration config = new AutomationConfiguration();

        config.setMinSimilarity(0.9);
        config.setMoveMouseDelay(1.0);
        config.setDelayBeforeMouseDown(0.1);
        config.setDelayAfterMouseDown(0.1);
        config.setDelayBeforeMouseUp(0.1);
        config.setDelayAfterMouseUp(0.1);
        config.setTypeDelay(0.1);
        config.setPauseBetweenActions(0.1);
        config.setMaxWait(5.0);
        config.setImageDirectory("/test/images");
        config.setLogLevel("DEBUG");
        config.setIllustrationEnabled(false);
        config.setAutomationFunctions(new ArrayList<>()); // Assuming AutomationFunction class exists

        assertEquals(0.9, config.getMinSimilarity());
        assertEquals(1.0, config.getMoveMouseDelay());
        assertEquals(0.1, config.getDelayBeforeMouseDown());
        assertEquals(0.1, config.getDelayAfterMouseDown());
        assertEquals(0.1, config.getDelayBeforeMouseUp());
        assertEquals(0.1, config.getDelayAfterMouseUp());
        assertEquals(0.1, config.getTypeDelay());
        assertEquals(0.1, config.getPauseBetweenActions());
        assertEquals(5.0, config.getMaxWait());
        assertEquals("/test/images", config.getImageDirectory());
        assertEquals("DEBUG", config.getLogLevel());
        assertFalse(config.getIllustrationEnabled());
        assertNotNull(config.getAutomationFunctions());
    }
}