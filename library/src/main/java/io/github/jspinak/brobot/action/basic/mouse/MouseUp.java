package io.github.jspinak.brobot.action.basic.mouse;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.mouse.MouseUpWrapper;

import org.springframework.stereotype.Component;

/**
 * Releases a mouse button in the Brobot model-based GUI automation framework.
 * 
 * <p>MouseUp is a low-level action in the Action Model (α) that releases a previously 
 * pressed mouse button. It completes mouse interactions initiated by MouseDown, enabling 
 * the framework to perform complex mouse gestures and maintain proper button state 
 * throughout automation execution.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li><b>Button Release</b>: Releases left, right, or middle mouse buttons</li>
 *   <li><b>Timing Control</b>: Configurable pauses before and after release for 
 *       precise interaction timing</li>
 *   <li><b>State Completion</b>: Completes drag operations and finalizes selections</li>
 *   <li><b>Event Generation</b>: Triggers appropriate mouse release events in the GUI</li>
 * </ul>
 * 
 * <p>Common use cases:</p>
 * <ul>
 *   <li>Completing drag-and-drop operations (after MouseDown and MouseMove)</li>
 *   <li>Finalizing selection rectangles for multiple item selection</li>
 *   <li>Ending context menu triggers started with right button MouseDown</li>
 *   <li>Completing custom gestures that require button press and release</li>
 * </ul>
 * 
 * <p>Important considerations:</p>
 * <ul>
 *   <li>Must be paired with a preceding MouseDown action</li>
 *   <li>Releasing an already-released button is typically harmless but ineffective</li>
 *   <li>The mouse position remains unchanged by this action</li>
 *   <li>Some applications may process the release event differently based on duration</li>
 * </ul>
 * 
 * <p>In the model-based approach, MouseUp represents the completion phase of mouse 
 * interactions. Together with MouseDown and MouseMove, it enables the decomposition 
 * of complex GUI gestures into atomic, reusable operations. This granular control 
 * is essential for handling sophisticated interaction patterns while maintaining 
 * the framework's ability to model and predict GUI behavior.</p>
 * 
 * @since 1.0
 * @see MouseDown
 * @see MouseMove
 * @see Click
 * @see ActionOptions
 */
@Component
public class MouseUp implements ActionInterface {

    private final MouseUpWrapper mouseUpWrapper;

    public MouseUp(MouseUpWrapper mouseUpWrapper) {
        this.mouseUpWrapper = mouseUpWrapper;
    }

    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        mouseUpWrapper.press(
                actionOptions.getPauseBeforeMouseUp(),
                actionOptions.getPauseAfterMouseUp(),
                actionOptions.getClickType());
    }

}
