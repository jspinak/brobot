package io.github.jspinak.brobot.action.internal.mouse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.basic.mouse.ScrollOptions;
import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.model.element.Region;

/**
 * Provides mouse wheel scrolling functionality with support for both real and mocked operations.
 *
 * <p>This is version 2 of the MouseWheel class, updated to work with the new ActionConfig hierarchy
 * and ScrollOptions instead of ActionConfig.
 *
 * <p>This wrapper abstracts the underlying Sikuli mouse wheel operations and integrates with
 * Brobot's action system. It handles scroll direction conversion and supports the mock mode for
 * testing purposes where actual scrolling is logged but not performed.
 *
 * @see ScrollOptions
 * @see BrobotProperties
 * @since 2.0
 */
@Component
public class MouseWheelScroller {

    private final BrobotProperties brobotProperties;

    @Autowired
    public MouseWheelScroller(BrobotProperties brobotProperties) {
        this.brobotProperties = brobotProperties;
    }

    // No longer needs legacy dependency - this class is now standalone

    /**
     * Performs mouse wheel scrolling based on the provided scroll options.
     *
     * <p>In mock mode, the action is logged but not actually performed, useful for testing. In
     * normal mode, creates a new {@link Region} at the current mouse position and performs the
     * scroll operation through Sikuli's wheel API.
     *
     * <p>The scroll direction is converted from the {@link ScrollOptions.Direction} enum to the
     * integer value expected by Sikuli (UP=-1, DOWN=1).
     *
     * @param scrollOptions Configuration containing scroll direction and number of scroll steps.
     *     Must not be null.
     * @return Always returns {@code true} to indicate the operation was attempted. Note: This does
     *     not guarantee the scroll was successful in real mode.
     */
    public boolean scroll(ScrollOptions scrollOptions) {
        if (brobotProperties.getCore().isMock()) {
            System.out.println(
                    "Mock: scroll "
                            + scrollOptions.getDirection()
                            + " "
                            + scrollOptions.getScrollSteps()
                            + " times.");
            return true;
        }

        // Convert Direction to Sikuli's expected integer values
        int scrollDirection = (scrollOptions.getDirection() == ScrollOptions.Direction.UP) ? -1 : 1;

        new Region().sikuli().wheel(scrollDirection, scrollOptions.getScrollSteps());
        return true;
    }
}
