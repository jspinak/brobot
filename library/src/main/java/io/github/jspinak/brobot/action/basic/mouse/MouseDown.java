package io.github.jspinak.brobot.action.basic.mouse;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.mouse.ClickType;
import io.github.jspinak.brobot.action.internal.mouse.MouseDownWrapper;

/**
 * Presses and holds a mouse button in the Brobot model-based GUI automation framework.
 *
 * <p>MouseDown is a low-level action in the Action Model (α) that initiates a mouse button press
 * without releasing it. This action is essential for implementing drag-and-drop operations, context
 * menu invocations, and other interactions that require sustained mouse button pressure.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li><b>Button Control</b>: Supports left, right, and middle mouse button presses
 *   <li><b>Timing Precision</b>: Configurable pauses before and after the button press to
 *       accommodate different application response times
 *   <li><b>State Persistence</b>: Maintains button state until a corresponding MouseUp action is
 *       performed
 *   <li><b>Platform Independence</b>: Works consistently across different operating systems
 * </ul>
 *
 * <p>Common use cases:
 *
 * <ul>
 *   <li>Starting drag-and-drop operations (followed by MouseMove and MouseUp)
 *   <li>Initiating selection rectangles for multiple item selection
 *   <li>Triggering context menus with right button press
 *   <li>Custom gesture recognition requiring sustained button pressure
 * </ul>
 *
 * <p>Important considerations:
 *
 * <ul>
 *   <li>Always pair with MouseUp to avoid leaving the mouse in a pressed state
 *   <li>The mouse position is not changed by this action
 *   <li>Some applications may have timeouts for sustained button presses
 *   <li>Platform-specific behaviors may affect long button holds
 * </ul>
 *
 * <p>In the model-based approach, MouseDown represents the initiation phase of complex mouse
 * interactions. It enables the framework to decompose compound gestures into atomic operations,
 * providing fine-grained control over GUI interactions while maintaining the ability to model
 * higher-level behavioral patterns.
 *
 * @since 1.0
 * @see MouseUp
 * @see MouseMove
 * @see Click
 * @see ActionConfig
 */
@Component
public class MouseDown implements ActionInterface {

    private final MouseDownWrapper mouseDownWrapper;

    public MouseDown(MouseDownWrapper mouseDownWrapper) {
        this.mouseDownWrapper = mouseDownWrapper;
    }

    @Override
    public Type getActionType() {
        return Type.MOUSE_DOWN;
    }

    @Override
    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        // Get the configuration - expecting MouseDownOptions
        if (matches.getActionConfig() instanceof MouseDownOptions) {
            MouseDownOptions options = (MouseDownOptions) matches.getActionConfig();
            mouseDownWrapper.press(
                    options.getPauseBeforeMouseDown(),
                    options.getPauseAfterMouseDown(),
                    convertMouseButton(options.getButton()));
        } else {
            // Fallback for other configs or throw exception
            throw new IllegalArgumentException("MouseDown requires MouseDownOptions configuration");
        }
    }

    /**
     * Converts MouseButton enum to ClickType.Type for backward compatibility. This conversion will
     * be removed once ClickType is fully replaced.
     */
    private ClickType.Type convertMouseButton(
            io.github.jspinak.brobot.model.action.MouseButton button) {
        switch (button) {
            case RIGHT:
                return ClickType.Type.RIGHT;
            case MIDDLE:
                return ClickType.Type.MIDDLE;
            case LEFT:
            default:
                return ClickType.Type.LEFT;
        }
    }
}
