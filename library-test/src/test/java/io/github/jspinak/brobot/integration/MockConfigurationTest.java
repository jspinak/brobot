package io.github.jspinak.brobot.integration;

import io.github.jspinak.brobot.test.BrobotTestBase;

import io.github.jspinak.brobot.config.core.FrameworkSettings;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that mock mode configuration is properly applied.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "brobot.core.mock=true",
        "brobot.mock.time-find-first=0.01"
})
public class MockConfigurationTest extends BrobotTestBase {

    @Test
    public void testMockModeIsEnabled() {
        // Wait a bit for configuration to be applied
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // ignore
        }

        System.out.println("=== MOCK CONFIGURATION TEST ===");
        System.out.println("FrameworkSettings.mock = " + FrameworkSettings.mock);
        System.out.println("FrameworkSettings.mockTimeFindFirst = " + FrameworkSettings.mockTimeFindFirst);

        assertTrue(FrameworkSettings.mock, "Mock mode should be enabled");
        assertEquals(0.01, FrameworkSettings.mockTimeFindFirst, 0.001,
                "Mock find time should be configured");

        System.out.println("✅ Mock mode configuration is working correctly!");
    }
}