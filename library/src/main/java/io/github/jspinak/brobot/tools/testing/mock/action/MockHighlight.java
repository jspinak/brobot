package io.github.jspinak.brobot.tools.testing.mock.action;

import java.util.List;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.element.Region;

import lombok.extern.slf4j.Slf4j;

/**
 * Mock implementation of highlight operations for testing.
 *
 * <p>This class provides simulated highlight functionality for use in mock mode, allowing tests to
 * run without actual screen interaction. It logs highlight operations without performing actual
 * screen highlighting.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Simulates highlight operations without screen interaction
 *   <li>Records highlight requests for verification in tests
 *   <li>Returns success for all valid highlight requests
 *   <li>Provides consistent behavior for test automation
 * </ul>
 *
 * @since 1.1.0
 * @see io.github.jspinak.brobot.action.basic.highlight.Highlight
 */
@Slf4j
@Component
public class MockHighlight {

    private int highlightCount = 0;
    private Region lastHighlightedRegion;

    /**
     * Simulates highlighting a single region.
     *
     * @param region the region to highlight
     * @param duration the duration in seconds (ignored in mock)
     * @param color the highlight color (ignored in mock)
     * @return true to indicate successful highlight simulation
     */
    public boolean highlightRegion(Region region, double duration, String color) {
        log.debug("Mock highlight: region={}, duration={}, color={}", region, duration, color);
        highlightCount++;
        lastHighlightedRegion = region;
        return true;
    }

    /**
     * Simulates highlighting multiple regions.
     *
     * @param regions the regions to highlight
     * @param duration the duration in seconds (ignored in mock)
     * @param color the highlight color (ignored in mock)
     * @return number of regions successfully highlighted (all in mock mode)
     */
    public int highlightRegions(List<Region> regions, double duration, String color) {
        log.debug(
                "Mock highlight: {} regions, duration={}, color={}",
                regions.size(),
                duration,
                color);
        for (Region region : regions) {
            highlightRegion(region, duration, color);
        }
        return regions.size();
    }

    /**
     * Gets the total number of highlight operations performed. Useful for test verification.
     *
     * @return the count of highlight operations
     */
    public int getHighlightCount() {
        return highlightCount;
    }

    /**
     * Gets the last region that was highlighted. Useful for test verification.
     *
     * @return the last highlighted region, or null if none
     */
    public Region getLastHighlightedRegion() {
        return lastHighlightedRegion;
    }

    /** Resets the mock state for testing. */
    public void reset() {
        highlightCount = 0;
        lastHighlightedRegion = null;
        log.debug("MockHighlight state reset");
    }
}
