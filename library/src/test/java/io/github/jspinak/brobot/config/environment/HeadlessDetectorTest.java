package io.github.jspinak.brobot.config.environment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

/**
 * Tests for HeadlessDetector.
 *
 * <p>Note: These tests are environment-dependent and may behave differently on different systems.
 */
class HeadlessDetectorTest {

    private HeadlessDetector detector;

    @BeforeEach
    void setUp() {
        // Create detector with headless=false and debug=true
        // This simulates normal Spring injection with default values
        detector = new HeadlessDetector(false, true);
    }

    @Test
    void testHeadlessDetection() {
        // This test will print debug info about the current environment
        boolean isHeadless = detector.isHeadless();
        System.out.println("\n" + detector.getStatusReport());

        // The actual assertion depends on the environment
        // We're just checking that it doesn't throw exceptions
        assertNotNull(detector.getStatusReport());
    }

    @Test
    void testConsistentResults() {
        // First call
        boolean firstResult = detector.isHeadless();

        // Second call should return the same result
        boolean secondResult = detector.isHeadless();

        // Results should be the same
        assertEquals(firstResult, secondResult);
    }

    @Test
    void testStatusReport() {
        // Get status report
        String report = detector.getStatusReport();

        // Report should not be null and should contain expected info
        assertNotNull(report);
        assertTrue(report.contains("HeadlessDetector Status"));
        assertTrue(report.contains("Current State"));
        assertTrue(report.contains("OS:"));
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void testWindowsWithDisplay() {
        // On Windows with a display, it should generally not be headless
        // unless running in CI or explicitly set
        String ci = System.getenv("CI");
        String headlessProp = System.getProperty("java.awt.headless");

        System.out.println("Windows test - CI: " + ci + ", headless prop: " + headlessProp);

        if (ci == null && !"true".equals(headlessProp)) {
            // Not in CI and not forced headless - should have display
            boolean isHeadless = detector.isHeadless();
            System.out.println("Windows headless detection result: " + isHeadless);
            System.out.println(detector.getStatusReport());

            // On normal Windows with monitor, should NOT be headless
            assertFalse(
                    isHeadless,
                    "Windows with display should not be detected as headless unless explicitly"
                            + " set");
        }
    }

    @Test
    @EnabledIfSystemProperty(named = "java.awt.headless", matches = "false")
    void testExplicitNonHeadless() {
        // When explicitly set to false, should not be headless
        boolean isHeadless = detector.isHeadless();
        assertFalse(isHeadless, "Should not be headless when java.awt.headless=false");
    }

    @Test
    @EnabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    void testExplicitHeadless() {
        // Create a detector with headless=true
        HeadlessDetector headlessDetector = new HeadlessDetector(true, false);
        boolean isHeadless = headlessDetector.isHeadless();
        assertTrue(isHeadless, "Should be headless when configured with headless=true");
    }

    @Test
    void testHeadlessModeConfiguration() {
        // Test with headless=true
        HeadlessDetector headlessDetector = new HeadlessDetector(true, false);
        assertTrue(headlessDetector.isHeadless(), "Should be headless when configured as true");

        // Test with headless=false
        HeadlessDetector guiDetector = new HeadlessDetector(false, false);
        assertFalse(guiDetector.isHeadless(), "Should not be headless when configured as false");
    }

    @Test
    void testForceNonHeadlessInitializer() {
        // Check if ForceNonHeadlessInitializer was triggered
        boolean wasForced = detector.wasForcedNonHeadless();

        // This will be true if the initializer had to override headless settings
        // The actual value depends on the environment
        System.out.println("ForceNonHeadlessInitializer forced mode: " + wasForced);

        // Just verify the method doesn't throw
        assertNotNull(Boolean.valueOf(wasForced));
    }
}
