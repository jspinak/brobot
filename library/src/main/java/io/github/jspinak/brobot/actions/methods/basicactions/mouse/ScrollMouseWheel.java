package io.github.jspinak.brobot.actions.methods.basicactions.mouse;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse.MouseWheel;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

/**
 * Scrolls the mouse wheel in the Brobot model-based GUI automation framework.
 * 
 * <p>ScrollMouseWheel is a fundamental mouse action in the Action Model (α) that simulates 
 * mouse wheel rotation for scrolling through content. It provides a natural way to navigate 
 * scrollable areas, lists, documents, and other content that extends beyond the visible 
 * viewport, mimicking standard user scrolling behavior.</p>
 * 
 * <p>Key features:
 * <ul>
 *   <li><b>Directional Control</b>: Supports both upward and downward scrolling</li>
 *   <li><b>Variable Speed</b>: Configurable scroll amounts for fine or coarse scrolling</li>
 *   <li><b>Platform Compatibility</b>: Works consistently across different operating systems 
 *       despite varying scroll implementations</li>
 *   <li><b>Focus-aware</b>: Scrolls within the window or element that currently has focus</li>
 * </ul>
 * </p>
 * 
 * <p>Common use cases:
 * <ul>
 *   <li>Navigating through long documents or web pages</li>
 *   <li>Scrolling through lists, tables, or dropdown menus</li>
 *   <li>Revealing hidden content in scrollable containers</li>
 *   <li>Adjusting zoom levels in applications that use wheel for zooming</li>
 *   <li>Triggering scroll-based animations or lazy-loaded content</li>
 * </ul>
 * </p>
 * 
 * <p>Configuration through ActionOptions:
 * <ul>
 *   <li>Scroll direction (up/down)</li>
 *   <li>Number of scroll units or "clicks"</li>
 *   <li>Scroll speed and acceleration</li>
 *   <li>Pause duration after scrolling</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, ScrollMouseWheel actions enable navigation through 
 * content-rich interfaces without relying on specific scrollbar implementations. This 
 * abstraction is particularly valuable for cross-platform automation where scrollbar 
 * appearance and behavior can vary significantly between applications and operating 
 * systems.</p>
 * 
 * <p>Note: Some applications may interpret mouse wheel events differently (e.g., for 
 * zooming instead of scrolling), and the framework accounts for these variations through 
 * its configuration options.</p>
 * 
 * @since 1.0
 * @see ActionOptions
 * @see MouseWheel
 * @see MoveMouse
 * @see Click
 */
@Component
public class ScrollMouseWheel implements ActionInterface {

    private final MouseWheel mouseWheel;

    public ScrollMouseWheel(MouseWheel mouseWheel) {
        this.mouseWheel = mouseWheel;
    }

    public void perform(Matches matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        mouseWheel.scroll(actionOptions);
    }
}
