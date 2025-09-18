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
        detector = new HeadlessDetector();
        detector.setDebugLogging(true);
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
    void testCaching() {
        // First call
        boolean firstResult = detector.isHeadless();

        // Second call should use cache (should be fast)
        long start = System.currentTimeMillis();
        boolean secondResult = detector.isHeadless();
        long duration = System.currentTimeMillis() - start;

        // Results should be the same
        assertEquals(firstResult, secondResult);

        // Cached call should be very fast (< 1ms)
        assertTrue(duration < 10, "Cached call took too long: " + duration + "ms");
    }

    @Test
    void testCacheRefresh() {
        // Get initial result
        boolean beforeRefresh = detector.isHeadless();

        // Refresh cache
        detector.refreshCache();

        // Get result after refresh (should recompute)
        boolean afterRefresh = detector.isHeadless();

        // Results should still be the same (environment hasn't changed)
        assertEquals(beforeRefresh, afterRefresh);
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
        // When explicitly set to true, should be headless
        boolean isHeadless = detector.isHeadless();
        assertTrue(isHeadless, "Should be headless when java.awt.headless=true");
    }
}
