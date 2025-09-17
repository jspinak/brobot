package io.github.jspinak.brobot.action.internal.utility;

import org.sikuli.basics.Settings;
import org.sikuli.script.FindFailed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.tools.history.IllustrationController;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.tools.testing.mock.action.MockDrag;
import io.github.jspinak.brobot.tools.testing.wrapper.TimeWrapper;

/**
 * Provides drag-and-drop functionality between locations with configurable timing and mock support.
 *
 * <p>This wrapper abstracts Sikuli's drag-and-drop operations, enabling controlled dragging from
 * one {@link Location} to another. It supports fine-grained timing control at each stage of the
 * drag operation (mouse down, drag movement, mouse up) to ensure compatibility with various
 * applications that may have different responsiveness requirements.
 *
 * <p>The class integrates with Brobot's action system to provide:
 *
 * <ul>
 *   <li>Configurable delays at each drag stage via {@link ActionConfig}
 *   <li>Mock support for testing without actual mouse movements
 *   <li>Detailed reporting of drag operations and failures
 * </ul>
 *
 * <p>Typical timing recommendations for reliable drag operations:
 *
 * <ul>
 *   <li>PauseBeforeMouseDown: 0.3s - Allow UI to settle before starting
 *   <li>PauseAfterMouseDown: 0.3s - Ensure drag is recognized
 *   <li>MoveMouseDelay: 0.5s - Control drag speed
 *   <li>PauseBeforeMouseUp: 0.4s - Allow drop target to activate
 *   <li>PauseAfterMouseUp: 0.0s - Usually no delay needed
 * </ul>
 *
 * @see Location
 * @see ActionConfig
 * @see MockDrag
 * @see Settings
 */
@Component
public class DragCoordinateCalculator {

    @Autowired private BrobotProperties brobotProperties;
    private final MockDrag mock;
    private final TimeWrapper timeWrapper;
    private IllustrationController illustrateScreenshot;

    public DragCoordinateCalculator(
            MockDrag mock, TimeWrapper timeWrapper, IllustrationController illustrateScreenshot) {
        this.mock = mock;
        this.timeWrapper = timeWrapper;
        this.illustrateScreenshot = illustrateScreenshot;
    }

    /**
     * Performs the low-level drag operation using Sikuli's dragDrop method.
     *
     * <p>This private method handles the actual drag execution and exception handling. It creates a
     * new {@link Region} to access Sikuli's drag functionality and catches {@link FindFailed}
     * exceptions that may occur during the operation.
     *
     * @param from The starting location for the drag
     * @param to The destination location for the drag
     * @return {@code true} if the drag succeeded, {@code false} if FindFailed occurred
     */
    private boolean drag(Location from, Location to) {
        try {
            new Region().sikuli().dragDrop(from.sikuli(), to.sikuli());
        } catch (FindFailed findFailed) {
            ConsoleReporter.print(ConsoleReporter.OutputLevel.HIGH, "|drag failed| ");
            return false;
        }
        return true;
    }

    /**
     * Executes a drag-and-drop operation with configurable timing parameters.
     *
     * <p>This method performs a complete drag-and-drop sequence from one location to another, with
     * precise timing control at each stage. The timing parameters from {@link ActionConfig} are
     * applied to Sikuli's global settings before the drag operation begins.
     *
     * <p>The method reports the drag coordinates at HIGH output level for debugging. In mock mode,
     * the actual drag is simulated by the mock provider.
     *
     * <p>The drag sequence consists of:
     *
     * <ol>
     *   <li>Pause before mouse down (UI settling time)
     *   <li>Mouse down at source location
     *   <li>Pause after mouse down (drag recognition time)
     *   <li>Move mouse to destination (with controlled speed)
     *   <li>Pause before mouse up (drop target activation)
     *   <li>Mouse up at destination
     *   <li>Pause after mouse up (post-action delay)
     * </ol>
     *
     * @param from The starting location for the drag. Must not be null.
     * @param to The destination location for the drag. Must not be null.
     * @param actionConfig Configuration containing all timing parameters for the drag stages.
     * @return {@code true} if the drag completed successfully (or was mocked), {@code false} if the
     *     drag operation failed.
     * @implNote This method modifies global Sikuli settings. While these are set before each drag,
     *     be aware that they affect all Sikuli operations until changed.
     */
    // Removed duplicate drag(ActionConfig) method - use the one below that properly
    // handles ActionConfig types

    /**
     * Executes a drag-and-drop operation using DragOptions configuration.
     *
     * <p>This method performs a complete drag-and-drop sequence using the new ActionConfig API. It
     * extracts timing parameters from the DragOptions and its contained MousePressOptions.
     *
     * @param from The starting location for the drag. Must not be null.
     * @param to The destination location for the drag. Must not be null.
     * @param dragOptions Configuration containing timing and mouse button settings.
     * @return {@code true} if the drag completed successfully (or was mocked), {@code false} if the
     *     drag operation failed.
     */
    public boolean drag(Location from, Location to, DragOptions dragOptions) {
        ConsoleReporter.format(
                ConsoleReporter.OutputLevel.HIGH,
                "drag %d.%d to %d.%d ",
                from.getCalculatedX(),
                from.getCalculatedY(),
                to.getCalculatedX(),
                to.getCalculatedY());
        if (brobotProperties.getCore().isMock()) return mock.drag();

        // Extract timing from MousePressOptions and DragOptions
        Settings.DelayBeforeMouseDown =
                dragOptions.getMousePressOptions().getPauseBeforeMouseDown();
        Settings.DelayBeforeDrag = dragOptions.getDelayBetweenMouseDownAndMove();
        Settings.MoveMouseDelay =
                brobotProperties
                        .getMouse()
                        .getMoveDelay(); // Use default as DragOptions doesn't have this
        Settings.DelayBeforeDrop = dragOptions.getMousePressOptions().getPauseBeforeMouseUp();

        if (!drag(from, to)) return false;
        timeWrapper.wait(dragOptions.getDelayAfterDrag());
        return true;
    }

    /**
     * Executes a drag-and-drop operation using ActionConfig.
     *
     * <p>This method provides support for ActionConfig types. Currently only DragOptions is
     * supported as ActionConfig doesn't extend ActionConfig.
     *
     * @param from The starting location for the drag.
     * @param to The destination location for the drag.
     * @param actionConfig Configuration for the drag operation.
     * @return {@code true} if the drag completed successfully, {@code false} otherwise.
     */
    public boolean drag(Location from, Location to, ActionConfig actionConfig) {
        if (actionConfig instanceof DragOptions) {
            return drag(from, to, (DragOptions) actionConfig);
        } else {
            ConsoleReporter.println(
                    "Unsupported ActionConfig type for drag: "
                            + actionConfig.getClass().getSimpleName());
            return false;
        }
    }
}
