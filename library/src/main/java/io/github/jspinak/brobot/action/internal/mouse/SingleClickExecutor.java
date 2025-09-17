package io.github.jspinak.brobot.action.internal.mouse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.model.action.MouseButton;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.tools.testing.wrapper.TimeWrapper;
import io.github.jspinak.brobot.util.coordinates.CoordinateScaler;

/**
 * Executes single mouse click operations at specified locations.
 *
 * <p>ClickLocationOnce provides a complete click implementation that handles the full lifecycle of
 * a mouse click: moving to the target, pressing down, and releasing. It supports various click
 * types (left, right, middle, double) and configurable timing between click phases.
 *
 * <p><strong>Key features:</strong>
 *
 * <ul>
 *   <li>Supports all mouse button types (left, right, middle)
 *   <li>Handles single and double clicks with proper timing
 *   <li>Configurable pauses before/after mouse down/up events
 *   <li>Mock mode support for testing without GUI interaction
 *   <li>Intelligent double-click optimization using native methods when possible
 * </ul>
 *
 * <p><strong>Click execution flow:</strong>
 *
 * <ol>
 *   <li>Move mouse to target location
 *   <li>Pause before mouse down (if configured)
 *   <li>Press mouse button down
 *   <li>Pause after mouse down (if configured)
 *   <li>Pause before mouse up (if configured)
 *   <li>Release mouse button
 *   <li>Pause after mouse up (if configured)
 *   <li>Repeat for double clicks if needed
 * </ol>
 *
 * <p><strong>Design rationale:</strong>
 *
 * <p>This wrapper abstracts the complexity of click operations, providing a single interface that
 * adapts to different click requirements. The separation of mouse down/up operations allows
 * fine-grained control over click timing, which is essential for applications that require specific
 * interaction patterns.
 *
 * @see MouseDownWrapper
 * @see MouseUpWrapper
 * @see MoveMouseWrapper
 * @see ClickType
 */
@Component
public class SingleClickExecutor {

    private final MouseDownWrapper mouseDownWrapper;
    private final MouseUpWrapper mouseUpWrapper;
    private final MoveMouseWrapper moveMouseWrapper;
    private final TimeWrapper timeWrapper;
    private final BrobotProperties brobotProperties;

    @Autowired private CoordinateScaler coordinateScaler;

    /**
     * Constructs a ClickLocationOnce with required mouse operation wrappers.
     *
     * @param mouseDownWrapper Handles mouse button press operations
     * @param mouseUpWrapper Handles mouse button release operations
     * @param moveMouseWrapper Handles mouse movement to target location
     */
    public SingleClickExecutor(
            MouseDownWrapper mouseDownWrapper,
            MouseUpWrapper mouseUpWrapper,
            MoveMouseWrapper moveMouseWrapper,
            TimeWrapper timeWrapper,
            BrobotProperties brobotProperties) {
        this.mouseDownWrapper = mouseDownWrapper;
        this.mouseUpWrapper = mouseUpWrapper;
        this.moveMouseWrapper = moveMouseWrapper;
        this.timeWrapper = timeWrapper;
        this.brobotProperties = brobotProperties;
    }

    /**
     * Performs a click operation at the specified location using ActionConfig.
     *
     * <p>This is the preferred method that accepts the new ActionConfig hierarchy. In mock mode,
     * simulates the click by printing to the report without actual mouse interaction. In normal
     * mode, executes the full click sequence including movement, button press, and release with
     * configured timing.
     *
     * @param location Target location for the click operation
     * @param config Configuration including click type and timing parameters
     * @return true if the click was performed successfully, false if movement failed
     */
    public boolean click(Location location, ActionConfig config) {
        if (!(config instanceof ClickOptions)) {
            // If not ClickOptions, fall back to simple left click
            return performClick(
                    location,
                    1,
                    MouseButton.LEFT,
                    config.getPauseBeforeBegin(),
                    config.getPauseAfterEnd(),
                    0,
                    0,
                    0,
                    0);
        }

        ClickOptions clickOptions = (ClickOptions) config;

        // Extract click parameters from ClickOptions
        int numberOfClicks = clickOptions.getNumberOfClicks();
        MouseButton button = clickOptions.getMousePressOptions().getButton();

        // Get timing parameters
        double pauseBeforeBegin = clickOptions.getPauseBeforeBegin();
        double pauseAfterEnd = clickOptions.getPauseAfterEnd();
        double pauseBeforeDown = clickOptions.getMousePressOptions().getPauseBeforeMouseDown();
        double pauseAfterDown = clickOptions.getMousePressOptions().getPauseAfterMouseDown();
        double pauseBeforeUp = clickOptions.getMousePressOptions().getPauseBeforeMouseUp();
        double pauseAfterUp = clickOptions.getMousePressOptions().getPauseAfterMouseUp();

        return performClick(
                location,
                numberOfClicks,
                button,
                pauseBeforeBegin,
                pauseAfterEnd,
                pauseBeforeDown,
                pauseAfterDown,
                pauseBeforeUp,
                pauseAfterUp);
    }

    /**
     * Performs a click operation at the specified location.
     *
     * <p>In mock mode, simulates the click by printing to the report without actual mouse
     * interaction. In normal mode, executes the full click sequence including movement, button
     * press, and release with configured timing.
     *
     * <p><strong>Side effects:</strong>
     *
     * <ul>
     *   <li>Moves the mouse cursor to the target location
     *   <li>Performs mouse button press and release
     *   <li>In mock mode, writes click information to Report
     * </ul>
     *
     * @param location Target location for the click operation
     * @deprecated This method has been removed. Use click(Location, ActionConfig) instead.
     */


    /**
     * Performs a click operation with full control over timing and button type.
     *
     * <p>This method provides the core click implementation that supports the new ActionConfig
     * architecture while maintaining backward compatibility.
     *
     * @param location Target location for the click
     * @param numberOfClicks Number of clicks to perform (1 for single, 2 for double)
     * @param button The mouse button to use
     * @param pauseBeforeBegin Pause before starting the click sequence
     * @param pauseAfterEnd Pause after completing the click sequence
     * @param pauseBeforeDown Pause before pressing the mouse button
     * @param pauseAfterDown Pause after pressing the mouse button
     * @param pauseBeforeUp Pause before releasing the mouse button
     * @param pauseAfterUp Pause after releasing the mouse button
     * @return true if the click was performed successfully, false if movement failed
     */
    private boolean performClick(
            Location location,
            int numberOfClicks,
            MouseButton button,
            double pauseBeforeBegin,
            double pauseAfterEnd,
            double pauseBeforeDown,
            double pauseAfterDown,
            double pauseBeforeUp,
            double pauseAfterUp) {
        if (brobotProperties.getCore().isMock()) {
            ConsoleReporter.print("<click>");
            if (button != MouseButton.LEFT || numberOfClicks > 1) {
                ConsoleReporter.print(button.name());
                if (numberOfClicks > 1) ConsoleReporter.print(" x" + numberOfClicks);
            }
            ConsoleReporter.print(" ");
            return true;
        }

        // Move to location
        if (!moveMouseWrapper.move(location)) return false;

        // Pause before beginning click sequence
        if (pauseBeforeBegin > 0) timeWrapper.wait(pauseBeforeBegin);

        // Check for native double-click optimization
        if (numberOfClicks == 2
                && button == MouseButton.LEFT
                && pauseBeforeDown == 0
                && pauseAfterDown == 0
                && pauseBeforeUp == 0
                && pauseAfterUp == 0) {
            // Use scaled coordinates for doubleClick
            org.sikuli.script.Location scaledLocation =
                    coordinateScaler.scaleLocationToLogical(location);
            scaledLocation.doubleClick();
        } else {
            // Perform clicks with timing
            if (numberOfClicks > 1) {
                ConsoleReporter.print(numberOfClicks + " clicks ");
            }

            // Convert MouseButton to legacy ClickType for wrapper compatibility
            ClickType.Type clickType = convertToClickType(button, numberOfClicks);

            for (int i = 0; i < numberOfClicks; i++) {
                mouseDownWrapper.press(pauseBeforeDown, pauseAfterDown, clickType);
                mouseUpWrapper.press(pauseBeforeUp, pauseAfterUp, clickType);
            }
        }

        // Pause after completing click sequence
        if (pauseAfterEnd > 0) timeWrapper.wait(pauseAfterEnd);

        return true;
    }

    /**
     * Converts MouseButton to legacy ClickType for backward compatibility.
     *
     * @param button The mouse button
     * @param numberOfClicks Number of clicks (used to determine if double-click)
     * @return The corresponding ClickType
     */
    private ClickType.Type convertToClickType(MouseButton button, int numberOfClicks) {
        if (numberOfClicks > 1) {
            switch (button) {
                case LEFT:
                    return ClickType.Type.DOUBLE_LEFT;
                case RIGHT:
                    return ClickType.Type.DOUBLE_RIGHT;
                case MIDDLE:
                    return ClickType.Type.DOUBLE_MIDDLE;
            }
        }
        switch (button) {
            case RIGHT:
                return ClickType.Type.RIGHT;
            case MIDDLE:
                return ClickType.Type.MIDDLE;
            default:
                return ClickType.Type.LEFT;
        }
    }

    /**
     * Converts ClickOptions.Type to legacy ClickType.Type for backward compatibility.
     *
     * @param clickOptionsType The ClickOptions type
     * @return The corresponding ClickType.Type
     */
    private ClickType.Type convertMouseButtonToClickType(MouseButton button) {
        switch (button) {
            case LEFT:
                return ClickType.Type.LEFT;
            case RIGHT:
                return ClickType.Type.RIGHT;
            case MIDDLE:
                return ClickType.Type.MIDDLE;
            default:
                return ClickType.Type.LEFT;
        }
    }

    /**
     * Checks if all click timing pauses are set to zero.
     *
     * <p>Used to determine if a native double-click can be used instead of two separate clicks with
     * timing control.
     *
     * @param clickOptions Configuration to check for pause settings
     * @return true if all pause values are zero, false otherwise
     */
    private boolean noPauses(ClickOptions clickOptions) {
        // Simplified ClickOptions doesn't have pause methods, return true for simple
        // behavior
        return true;
    }

    /**
     * Determines if a native double-click operation can be used.
     *
     * <p>Native double-clicks have optimal timing for the operating system and are preferred when
     * no custom timing is required. If any pauses are configured, the method returns false to force
     * manual click sequences that respect the custom timing.
     *
     * @param clickOptions Configuration containing click type and pause settings
     * @return true if native double-click should be used, false for manual clicks
     */
    private boolean isSimpleLeftDoubleClick(ClickOptions clickOptions) {
        return clickOptions.getNumberOfClicks() == 2
                && clickOptions.getMousePressOptions().getButton() == MouseButton.LEFT
                && noPauses(clickOptions);
    }

    /**
     * Checks if the click type represents a single click operation.
     *
     * <p>Single clicks include standard left, right, or middle button clicks, excluding any
     * double-click variants.
     *
     * @param clickOptions Configuration containing the click type
     * @return true if the action is a single click, false for double-clicks
     */
    private boolean isSingleClick(ClickOptions clickOptions) {
        return clickOptions.getNumberOfClicks() == 1;
    }

    /**
     * Determines if two separate click operations should be performed.
     *
     * <p>Two clicks are required in these cases:
     *
     * <ul>
     *   <li>Double right-click (no native support in Sikuli)
     *   <li>Double middle-click (no native support in Sikuli)
     *   <li>Double left-click with custom pause timing
     * </ul>
     *
     * <p>This method ensures proper handling of all double-click variants, working around platform
     * limitations for non-left double-clicks.
     *
     * @param clickOptions Configuration containing click type and timing
     * @return true if two separate clicks are needed, false otherwise
     */
    private boolean isTwoClicks(ClickOptions clickOptions) {
        if (clickOptions.getNumberOfClicks() != 2) return false;

        MouseButton button = clickOptions.getMousePressOptions().getButton();
        // Two separate clicks needed for double right or middle (no native support)
        if (button == MouseButton.RIGHT || button == MouseButton.MIDDLE) {
            return true;
        }
        // For double left, use two clicks only if custom pauses are configured
        return button == MouseButton.LEFT && !noPauses(clickOptions);
    }
}
