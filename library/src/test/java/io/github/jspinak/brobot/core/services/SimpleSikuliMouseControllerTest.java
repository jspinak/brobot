package io.github.jspinak.brobot.core.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.DisabledInCI;

/** Simplified test for SikuliMouseController using Brobot's mock framework. */
@DisabledInCI
public class SimpleSikuliMouseControllerTest extends BrobotTestBase {

    @Test
    void testBasicFunctionality() {
        // Create controller - should work in mock mode
        SikuliMouseController controller = new SikuliMouseController(null);

        // Test basic operations don't throw exceptions in mock mode
        assertDoesNotThrow(() -> controller.moveTo(100, 100));
        assertDoesNotThrow(() -> controller.click(200, 200, MouseController.MouseButton.LEFT));

        // Test implementation name
        assertEquals("Sikuli", controller.getImplementationName());
    }
}
