package io.github.jspinak.brobot.action.basic.mouse;
import io.github.jspinak.brobot.action.ActionType;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.mouse.MouseWheelV2;

import org.springframework.stereotype.Component;

/**
 * Scrolls the mouse wheel in the Brobot model-based GUI automation framework.
 * 
 * <p>ScrollMouseWheel is a fundamental mouse action in the Action Model (α) that simulates 
 * mouse wheel rotation for scrolling through content. It provides a natural way to navigate 
 * scrollable areas, lists, documents, and other content that extends beyond the visible 
 * viewport, mimicking standard user scrolling behavior.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li><b>Directional Control</b>: Supports both upward and downward scrolling</li>
 *   <li><b>Variable Speed</b>: Configurable scroll amounts for fine or coarse scrolling</li>
 *   <li><b>Platform Compatibility</b>: Works consistently across different operating systems 
 *       despite varying scroll implementations</li>
 *   <li><b>Focus-aware</b>: Scrolls within the window or element that currently has focus</li>
 * </ul>
 * 
 * <p>Common use cases:</p>
 * <ul>
 *   <li>Navigating through long documents or web pages</li>
 *   <li>Scrolling through lists, tables, or dropdown menus</li>
 *   <li>Revealing hidden content in scrollable containers</li>
 *   <li>Adjusting zoom levels in applications that use wheel for zooming</li>
 *   <li>Triggering scroll-based animations or lazy-loaded content</li>
 * </ul>
 * 
 * <p>Configuration through ScrollOptions:</p>
 * <ul>
 *   <li>Scroll direction (up/down)</li>
 *   <li>Number of scroll steps or "clicks"</li>
 *   <li>Pause duration after scrolling</li>
 * </ul>
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
 * @see ScrollOptions
 * @see MouseWheelV2
 * @see MoveMouse
 * @see Click
 */
@Component
public class ScrollMouseWheel implements ActionInterface {

    @Override
    public Type getActionType() {
        return Type.SCROLL_MOUSE_WHEEL;
    }

    private final MouseWheelV2 mouseWheel;

    public ScrollMouseWheel(MouseWheelV2 mouseWheel) {
        this.mouseWheel = mouseWheel;
    }

    @Override
    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        // Get the configuration - expecting ScrollOptions
        if (!(matches.getActionConfig() instanceof ScrollOptions)) {
            throw new IllegalArgumentException("ScrollMouseWheel requires ScrollOptions configuration");
        }
        ScrollOptions scrollOptions = (ScrollOptions) matches.getActionConfig();
        
        mouseWheel.scroll(scrollOptions);
    }
}
