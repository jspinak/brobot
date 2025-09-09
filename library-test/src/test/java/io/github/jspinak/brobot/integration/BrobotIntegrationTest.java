package io.github.jspinak.brobot.integration;

import io.github.jspinak.brobot.test.BrobotTestBase;

import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that Brobot library's auto-configuration is working correctly.
 */
@SpringBootTest
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Integration test requires non-CI environment")
public class BrobotIntegrationTest extends BrobotTestBase {

    @Autowired
    private BrobotProperties brobotProperties;

    @Test
    public void testBrobotPropertiesAreLoaded() {
        System.out.println("\n=== BROBOT INTEGRATION TEST ===");

        // Check that BrobotProperties bean is available
        assertNotNull(brobotProperties, "BrobotProperties should be autowired");

        // Check that properties are loaded from application.properties
        System.out.println("BrobotProperties values:");
        System.out.println("  core.mock = " + brobotProperties.getCore().isMock());
        System.out.println("  mock.time-find-first = " + brobotProperties.getMock().getTimeFindFirst());

        // Properties should match what's in application.properties
        assertTrue(brobotProperties.getCore().isMock(), "Mock mode should be enabled from application.properties");
        assertEquals(0.01, brobotProperties.getMock().getTimeFindFirst(), 0.001,
                "Mock find time should match application.properties");

        System.out.println("\n✅ Brobot library auto-configuration is working!");
    }

    @Test
    public void testFrameworkSettingsAreConfigured() {
        System.out.println("\n=== FRAMEWORK SETTINGS TEST ===");

        // Wait a bit for initialization
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // ignore
        }

        // Check that FrameworkSettings are updated from properties
        System.out.println("FrameworkSettings values:");
        System.out.println("  mock = " + FrameworkSettings.mock);
        System.out.println("  mockTimeFindFirst = " + FrameworkSettings.mockTimeFindFirst);

        assertTrue(FrameworkSettings.mock, "FrameworkSettings.mock should be true");
        assertEquals(0.01, FrameworkSettings.mockTimeFindFirst, 0.001,
                "FrameworkSettings mock time should be configured");

        System.out.println("\n✅ FrameworkSettings are properly configured from Brobot library!");
    }
}