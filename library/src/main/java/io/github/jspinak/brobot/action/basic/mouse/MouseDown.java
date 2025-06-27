package io.github.jspinak.brobot.action.basic.mouse;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.mouse.MouseDownWrapper;

import org.springframework.stereotype.Component;

/**
 * Presses and holds a mouse button in the Brobot model-based GUI automation framework.
 * 
 * <p>MouseDown is a low-level action in the Action Model (α) that initiates a mouse button 
 * press without releasing it. This action is essential for implementing drag-and-drop 
 * operations, context menu invocations, and other interactions that require sustained 
 * mouse button pressure.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li><b>Button Control</b>: Supports left, right, and middle mouse button presses</li>
 *   <li><b>Timing Precision</b>: Configurable pauses before and after the button press 
 *       to accommodate different application response times</li>
 *   <li><b>State Persistence</b>: Maintains button state until a corresponding MouseUp 
 *       action is performed</li>
 *   <li><b>Platform Independence</b>: Works consistently across different operating systems</li>
 * </ul>
 * 
 * <p>Common use cases:</p>
 * <ul>
 *   <li>Starting drag-and-drop operations (followed by MouseMove and MouseUp)</li>
 *   <li>Initiating selection rectangles for multiple item selection</li>
 *   <li>Triggering context menus with right button press</li>
 *   <li>Custom gesture recognition requiring sustained button pressure</li>
 * </ul>
 * 
 * <p>Important considerations:</p>
 * <ul>
 *   <li>Always pair with MouseUp to avoid leaving the mouse in a pressed state</li>
 *   <li>The mouse position is not changed by this action</li>
 *   <li>Some applications may have timeouts for sustained button presses</li>
 *   <li>Platform-specific behaviors may affect long button holds</li>
 * </ul>
 * 
 * <p>In the model-based approach, MouseDown represents the initiation phase of complex 
 * mouse interactions. It enables the framework to decompose compound gestures into 
 * atomic operations, providing fine-grained control over GUI interactions while 
 * maintaining the ability to model higher-level behavioral patterns.</p>
 * 
 * @since 1.0
 * @see MouseUp
 * @see MouseMove
 * @see Click
 * @see ActionOptions
 */
@Component
public class MouseDown implements ActionInterface {

    private final MouseDownWrapper mouseDownWrapper;

    public MouseDown(MouseDownWrapper mouseDownWrapper) {
        this.mouseDownWrapper = mouseDownWrapper;
    }

    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        mouseDownWrapper.press(
                actionOptions.getPauseBeforeMouseDown(),
                actionOptions.getPauseAfterMouseDown(),
                actionOptions.getClickType());
    }

}
