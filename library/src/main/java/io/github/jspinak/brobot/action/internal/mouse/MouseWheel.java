package io.github.jspinak.brobot.action.internal.mouse;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.mouse.ScrollOptions;
import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides mouse wheel scrolling functionality with support for both real and mocked operations.
 * <p>
 * This wrapper abstracts the underlying Sikuli mouse wheel operations and integrates
 * with Brobot's action system. It handles scroll direction conversion and supports
 * the mock mode for testing purposes where actual scrolling is logged but not performed.
 * <p>
 * The class converts scroll direction enum values to the integer
 * values expected by the underlying Sikuli API (UP = -1, DOWN = 1).
 * @see FrameworkSettings#mock
 */
@Component
public class MouseWheel {

    public enum ScrollDirection {
        UP, DOWN
    }
    
    private Map<ScrollDirection, Integer> scrollInt = new HashMap<>();
    {
        scrollInt.put(ScrollDirection.DOWN, 1);
        scrollInt.put(ScrollDirection.UP, -1);
    }

    /**
     * Performs mouse wheel scrolling based on the provided action options.
     * <p>
     * In mock mode, the action is logged but not actually performed, useful for testing.
     * In normal mode, creates a new {@link Region} at the current mouse position and
     * performs the scroll operation through Sikuli's wheel API.
     * <p>
     * The scroll direction is converted from the {@link ScrollDirection} enum
     * to the integer value expected by Sikuli (UP=-1, DOWN=1). The number of scroll steps
     * is determined by the ScrollOptions configuration.
     * 
     * @param scrollOptions Configuration containing scroll direction and number of scroll steps.
     *                      Must not be null and must contain a valid scroll direction.
     * @return Always returns {@code true} to indicate the operation was attempted.
     *         Note: This does not guarantee the scroll was successful in real mode.
     * 
     * @see Region#sikuli()
     * @see ScrollDirection
     */
    public boolean scroll(ScrollOptions scrollOptions) {
        if (FrameworkSettings.mock) {
            ConsoleReporter.format("%s %d %s", "scroll", scrollOptions.getScrollSteps(), "times. ");
            return true;
        }
        ScrollDirection direction = scrollOptions.getDirection() == ScrollOptions.Direction.UP ? 
            ScrollDirection.UP : ScrollDirection.DOWN;
        new Region().sikuli().wheel(
                scrollInt.get(direction),
                scrollOptions.getScrollSteps()
        );
        return true;
    }
}
