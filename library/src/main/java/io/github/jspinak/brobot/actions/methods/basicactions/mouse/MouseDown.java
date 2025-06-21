package io.github.jspinak.brobot.actions.methods.basicactions.mouse;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse.MouseDownWrapper;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

/**
 * Presses and holds a mouse button in the Brobot model-based GUI automation framework.
 * 
 * <p>MouseDown is a low-level action in the Action Model (α) that initiates a mouse button 
 * press without releasing it. This action is essential for implementing drag-and-drop 
 * operations, context menu invocations, and other interactions that require sustained 
 * mouse button pressure.</p>
 * 
 * <p>Key features:
 * <ul>
 *   <li><b>Button Control</b>: Supports left, right, and middle mouse button presses</li>
 *   <li><b>Timing Precision</b>: Configurable pauses before and after the button press 
 *       to accommodate different application response times</li>
 *   <li><b>State Persistence</b>: Maintains button state until a corresponding MouseUp 
 *       action is performed</li>
 *   <li><b>Platform Independence</b>: Works consistently across different operating systems</li>
 * </ul>
 * </p>
 * 
 * <p>Common use cases:
 * <ul>
 *   <li>Starting drag-and-drop operations (followed by MouseMove and MouseUp)</li>
 *   <li>Initiating selection rectangles for multiple item selection</li>
 *   <li>Triggering context menus with right button press</li>
 *   <li>Custom gesture recognition requiring sustained button pressure</li>
 * </ul>
 * </p>
 * 
 * <p>Important considerations:
 * <ul>
 *   <li>Always pair with MouseUp to avoid leaving the mouse in a pressed state</li>
 *   <li>The mouse position is not changed by this action</li>
 *   <li>Some applications may have timeouts for sustained button presses</li>
 *   <li>Platform-specific behaviors may affect long button holds</li>
 * </ul>
 * </p>
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

    public void perform(Matches matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        mouseDownWrapper.press(
                actionOptions.getPauseBeforeMouseDown(),
                actionOptions.getPauseAfterMouseDown(),
                actionOptions.getClickType());
    }

}
