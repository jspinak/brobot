package io.github.jspinak.brobot.action.internal.mouse;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;

import org.sikuli.script.Mouse;
import org.springframework.stereotype.Component;

/**
 * Provides mouse button release functionality with timing control and mock support.
 * <p>
 * This wrapper abstracts Sikuli's mouse release operations, allowing controlled
 * release of mouse buttons that were previously pressed with MouseDownWrapper.
 * It supports all three mouse buttons (left, right, middle) and includes
 * configurable timing delays before and after the release action.
 * <p>
 * In mock mode, the action is logged for testing purposes without performing
 * actual mouse operations. The class works in conjunction with {@link MouseDownWrapper}
 * to enable drag-and-drop and other complex mouse interactions.
 * 
 * @see MouseDownWrapper
 * @see ClickType
 * @see Mouse#up(int)
 * @see FrameworkSettings#mock
 */
@Component
public class MouseUpWrapper {

    private final ClickType clickType;
    private final TimeProvider time;

    public MouseUpWrapper(ClickType clickType, TimeProvider time) {
        this.clickType = clickType;
        this.time = time;
    }

    /**
     * Releases the specified mouse button with configurable timing delays.
     * <p>
     * This method releases a previously pressed mouse button, typically used
     * as part of drag-and-drop operations or custom click sequences. The timing
     * delays allow for precise control over the release timing, which can be
     * important for compatibility with different applications.
     * <p>
     * In mock mode, the operation is logged with special notation for non-left
     * button releases. The actual mouse operation is skipped in mock mode.
     * <p>
     * Note: The method name "press" is misleading as it actually releases the button.
     * This appears to be a legacy naming issue that should be addressed in future versions.
     * 
     * @param pauseBefore Delay in seconds before releasing the button. Useful for
     *                    ensuring the target application is ready to receive the event.
     * @param pauseAfter Delay in seconds after releasing the button. Allows the
     *                   application time to process the release event.
     * @param type The mouse button to release (LEFT, RIGHT, or MIDDLE).
     * @return Always returns {@code true} to indicate the operation was attempted.
     *         Does not indicate whether the release was successful in real mode.
     * 
     * @see ClickType.Type
     * @see TimeProvider#wait(double)
     */
    public boolean press(double pauseBefore, double pauseAfter, ClickType.Type type) {
        if (FrameworkSettings.mock) {
            ConsoleReporter.print("<mouse-up>"); // this could be expanded if object clicks are given mock actions
            if (type != ClickType.Type.LEFT) System.out.print(type);
            System.out.print(" ");
            return true;
        }
        time.wait(pauseBefore);
        Mouse.up(clickType.getTypeToSikuliButton().get(type));
        time.wait(pauseAfter);
        return true;
    }

}
