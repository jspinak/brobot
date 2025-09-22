package io.github.jspinak.brobot.tools.testing.wrapper;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.config.environment.ExecutionMode;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.tools.testing.mock.action.MockHighlight;

import lombok.extern.slf4j.Slf4j;

/**
 * Wrapper for highlight operations that routes to mock or live implementation.
 *
 * <p>This wrapper provides a low-level routing mechanism for highlight operations, directing
 * execution to either mock or live implementations based on the current execution mode. It does not
 * depend on high-level Action classes, preventing circular dependencies.
 *
 * <h2>Architecture Pattern:</h2>
 *
 * <p>The wrapper pattern follows these principles:
 *
 * <ul>
 *   <li>Wrappers are low-level directors that choose between mock and live implementations
 *   <li>They do not depend on high-level Action classes
 *   <li>Action classes delegate to specific implementation classes, which use wrappers
 *   <li>This prevents circular dependencies in the injection chain
 * </ul>
 *
 * <h2>Dependency Flow:</h2>
 *
 * <pre>
 // * Action → ActionExecution → Highlight → HighlightManager → HighlightWrapper // HighlightManager removed
 *                                                                    ↓
 *                                                      MockHighlight / Live Sikuli
 * </pre>
 *
 * @see MockHighlight for mock implementation
 * @see ExecutionMode for execution mode detection
 * @since 1.1.0
 */
@Slf4j
@Component
public class HighlightWrapper {

    private final ExecutionMode executionMode;
    private final MockHighlight mockHighlight;

    /**
     * Constructs a HighlightWrapper with required dependencies.
     *
     * @param executionMode determines whether to use mock or live implementation
     * @param mockHighlight mock implementation for testing scenarios
     */
    @Autowired
    public HighlightWrapper(ExecutionMode executionMode, MockHighlight mockHighlight) {
        this.executionMode = executionMode;
        this.mockHighlight = mockHighlight;
    }

    /**
     * Highlights a single region, routing to mock or live implementation.
     *
     * @param region the region to highlight
     * @param duration highlight duration in seconds
     * @param color highlight color (e.g., "red", "green", "blue")
     * @return true if highlight was successful, false otherwise
     */
    public boolean highlightRegion(Region region, double duration, String color) {
        if (executionMode.isMock()) {
            log.debug("Mock mode: simulating highlight for region");
            return mockHighlight.highlightRegion(region, duration, color);
        }

        try {
            log.debug("Live mode: performing actual highlight for region");
            org.sikuli.script.Region sikuliRegion = region.sikuli();

            // Set color if specified
            if (color != null && !color.isEmpty()) {
                sikuliRegion.highlight(duration, color);
            } else {
                sikuliRegion.highlight(duration);
            }

            return true;
        } catch (Exception e) {
            log.error("Failed to highlight region: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Highlights a single region with default settings.
     *
     * @param region the region to highlight
     * @return true if highlight was successful, false otherwise
     */
    public boolean highlightRegion(Region region) {
        return highlightRegion(region, 2.0, "red");
    }

    /**
     * Highlights multiple regions, routing to mock or live implementation.
     *
     * @param regions the regions to highlight
     * @param duration highlight duration in seconds
     * @param color highlight color
     * @return number of regions successfully highlighted
     */
    public int highlightRegions(List<Region> regions, double duration, String color) {
        if (executionMode.isMock()) {
            log.debug("Mock mode: simulating highlight for {} regions", regions.size());
            return mockHighlight.highlightRegions(regions, duration, color);
        }

        log.debug("Live mode: highlighting {} regions", regions.size());
        int successCount = 0;
        for (Region region : regions) {
            if (highlightRegion(region, duration, color)) {
                successCount++;
            }
        }
        return successCount;
    }

    /**
     * Checks if the wrapper is in mock mode.
     *
     * @return true if in mock mode, false for live mode
     */
    public boolean isMockMode() {
        return executionMode.isMock();
    }

    /**
     * Gets a string representation of the wrapper state for debugging.
     *
     * @return string describing the wrapper state
     */
    @Override
    public String toString() {
        return String.format("HighlightWrapper[mode=%s]", executionMode.isMock() ? "mock" : "live");
    }
}
