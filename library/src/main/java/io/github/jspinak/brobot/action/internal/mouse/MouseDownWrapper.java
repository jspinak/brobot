package io.github.jspinak.brobot.action.internal.mouse;

import org.sikuli.script.Mouse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.tools.testing.wrapper.TimeWrapper;

/**
 * Provides mouse button press-and-hold functionality with timing control and mock support.
 *
 * <p>This wrapper abstracts Sikuli's mouse press operations, allowing controlled pressing of mouse
 * buttons that remain held until released with {@link MouseUpWrapper}. It supports all three mouse
 * buttons (left, right, middle) and includes configurable timing delays for precise control over
 * the press duration.
 *
 * <p>Common use cases include:
 *
 * <ul>
 *   <li>Drag-and-drop operations (press, move, release)
 *   <li>Context menu activation with timing requirements
 *   <li>Custom click sequences beyond standard clicks
 * </ul>
 *
 * <p>In mock mode, the action is logged for testing purposes without performing actual mouse
 * operations.
 *
 * @see MouseUpWrapper
 * @see ClickType
 * @see Mouse#down(int)
 * @see BrobotProperties
 */
@Component
public class MouseDownWrapper {

    private final BrobotProperties brobotProperties;
    private final ClickType clickType;
    private final TimeWrapper timeWrapper;

    @Autowired
    public MouseDownWrapper(
            BrobotProperties brobotProperties, ClickType clickType, TimeWrapper timeWrapper) {
        this.brobotProperties = brobotProperties;
        this.clickType = clickType;
        this.timeWrapper = timeWrapper;
    }

    /**
     * Presses and holds the specified mouse button with configurable timing.
     *
     * <p>This method presses a mouse button and keeps it held down for the specified duration. The
     * button remains pressed after this method returns and must be explicitly released using {@link
     * MouseUpWrapper#press}. This enables complex mouse interactions like drag-and-drop.
     *
     * <p>The timing parameters allow for:
     *
     * <ul>
     *   <li>Initial delay before pressing (for UI responsiveness)
     *   <li>Hold duration while the button remains pressed
     * </ul>
     *
     * <p>In mock mode, the operation is logged with special notation for non-left button presses.
     * The actual mouse operation is skipped in mock mode.
     *
     * @param pauseBeforeBegin Delay in seconds before pressing the button. Useful for ensuring the
     *     UI is ready to receive the event.
     * @param totalPause Duration in seconds to keep the button pressed. After this time, the method
     *     returns but the button remains pressed.
     * @param type The mouse button to press (LEFT, RIGHT, or MIDDLE).
     * @return Always returns {@code true} to indicate the operation was attempted. Does not
     *     indicate whether the press was successful in real mode.
     * @see MouseUpWrapper#press
     * @see ClickType.Type
     * @see TimeWrapper#wait(double)
     */
    public boolean press(double pauseBeforeBegin, double totalPause, ClickType.Type type) {
        if (brobotProperties.getCore().isMock()) {
            ConsoleReporter.print(
                    "<mouse-down>"); // this could be expanded if object clicks are given mock
            // actions
            if (type != ClickType.Type.LEFT) ConsoleReporter.print(type.name());
            ConsoleReporter.print(" ");
            return true;
        }
        timeWrapper.wait(pauseBeforeBegin);
        Mouse.down(clickType.getTypeToSikuliButton().get(type));
        timeWrapper.wait(totalPause);
        return true;
    }
}
