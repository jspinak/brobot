package io.github.jspinak.brobot.config.environment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for HeadlessDetector.
 *
 * <p>Tests the simplified configuration-based headless detection.
 */
class HeadlessDetectorTest {

    @Test
    void testHeadlessFalseConfiguration() {
        // Test with headless=false (default)
        HeadlessDetector detector = new HeadlessDetector(false);

        assertFalse(detector.isHeadless(), "Should not be headless when configured as false");
        assertEquals("Configured as GUI mode", detector.getStatusReport());
    }

    @Test
    void testHeadlessTrueConfiguration() {
        // Test with headless=true
        HeadlessDetector detector = new HeadlessDetector(true);

        assertTrue(detector.isHeadless(), "Should be headless when configured as true");
        assertEquals("Configured as HEADLESS", detector.getStatusReport());
    }

    @Test
    void testConsistentResults() {
        HeadlessDetector detector = new HeadlessDetector(false);

        // First call
        boolean firstResult = detector.isHeadless();

        // Multiple calls should return the same result
        for (int i = 0; i < 10; i++) {
            assertEquals(
                    firstResult,
                    detector.isHeadless(),
                    "Result should be consistent across multiple calls");
        }
    }

    @Test
    void testStatusReport() {
        // Test GUI mode status
        HeadlessDetector guiDetector = new HeadlessDetector(false);
        String guiReport = guiDetector.getStatusReport();
        assertNotNull(guiReport);
        assertTrue(guiReport.contains("GUI"));

        // Test headless mode status
        HeadlessDetector headlessDetector = new HeadlessDetector(true);
        String headlessReport = headlessDetector.getStatusReport();
        assertNotNull(headlessReport);
        assertTrue(headlessReport.contains("HEADLESS"));
    }

    @Test
    void testSimplifiedBehavior() {
        // Verify that the detector uses only the configured value
        // and doesn't try to detect the actual environment

        // Even if we're actually in a GUI environment,
        // if configured as headless, it should report headless
        HeadlessDetector detector = new HeadlessDetector(true);
        assertTrue(
                detector.isHeadless(),
                "Should use configured value regardless of actual environment");
    }
}
