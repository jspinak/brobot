package io.github.jspinak.brobot.integration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.test.BrobotTestBase;

/** Test to verify that mock mode configuration is properly applied. */
@SpringBootTest
public class MockConfigurationTest extends BrobotTestBase {

    @Autowired private BrobotProperties brobotProperties;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        // Set mock configuration
        // Mock mode enabled via @SpringBootTest;
        // BrobotProperties removed - use BrobotProperties: mockTimeFindFirst = 0.01;
    }

    @Test
    public void testMockModeIsEnabled() {
        // Wait a bit for configuration to be applied
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // ignore
        }

        System.out.println("=== MOCK CONFIGURATION TEST ===");
        System.out.println("mock = " + brobotProperties.getCore().isMock());
        System.out.println("mockTimeFindFirst = " + brobotProperties.getMock().getTimeFindFirst());

        assertTrue(brobotProperties.getCore().isMock(), "Mock mode should be enabled");
        assertEquals(
                0.01,
                brobotProperties.getMock().getTimeFindFirst(),
                0.001,
                "Mock find time should be configured");

        System.out.println("✅ Mock mode configuration is working correctly!");
    }
}
