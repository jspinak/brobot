package io.github.jspinak.brobot.action.internal.utility;

import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.tools.history.IllustrationController;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.tools.testing.mock.action.MockDrag;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;

import org.sikuli.basics.Settings;
import org.sikuli.script.FindFailed;
import org.springframework.stereotype.Component;

/**
 * Provides drag-and-drop functionality between locations with configurable timing and mock support.
 * <p>
 * This wrapper abstracts Sikuli's drag-and-drop operations, enabling controlled
 * dragging from one {@link Location} to another. It supports fine-grained timing
 * control at each stage of the drag operation (mouse down, drag movement, mouse up)
 * to ensure compatibility with various applications that may have different
 * responsiveness requirements.
 * <p>
 * The class integrates with Brobot's action system to provide:
 * <ul>
 * <li>Configurable delays at each drag stage via {@link ActionOptions}</li>
 * <li>Mock support for testing without actual mouse movements</li>
 * <li>Detailed reporting of drag operations and failures</li>
 * </ul>
 * <p>
 * Typical timing recommendations for reliable drag operations:
 * <ul>
 * <li>PauseBeforeMouseDown: 0.3s - Allow UI to settle before starting</li>
 * <li>PauseAfterMouseDown: 0.3s - Ensure drag is recognized</li>
 * <li>MoveMouseDelay: 0.5s - Control drag speed</li>
 * <li>PauseBeforeMouseUp: 0.4s - Allow drop target to activate</li>
 * <li>PauseAfterMouseUp: 0.0s - Usually no delay needed</li>
 * </ul>
 * 
 * @see Location
 * @see ActionOptions
 * @see MockDrag
 * @see Settings
 */
@Component
public class DragCoordinateCalculator {
    private final MockDrag mock;
    private final TimeProvider time;
    private IllustrationController illustrateScreenshot;

    public DragCoordinateCalculator(MockDrag mock, TimeProvider time, IllustrationController illustrateScreenshot) {
        this.mock = mock;
        this.time = time;
        this.illustrateScreenshot = illustrateScreenshot;
    }

    /**
     * Performs the low-level drag operation using Sikuli's dragDrop method.
     * <p>
     * This private method handles the actual drag execution and exception handling.
     * It creates a new {@link Region} to access Sikuli's drag functionality and
     * catches {@link FindFailed} exceptions that may occur during the operation.
     * 
     * @param from The starting location for the drag
     * @param to The destination location for the drag
     * @return {@code true} if the drag succeeded, {@code false} if FindFailed occurred
     */
    private boolean drag(Location from, Location to) {
        try {
            new Region().sikuli().dragDrop(from.sikuli(), to.sikuli());
        } catch (FindFailed findFailed) {
            if (ConsoleReporter.minReportingLevel(ConsoleReporter.OutputLevel.HIGH))
                System.out.print("|drag failed| ");
            return false;
        }
        return true;
    }

    /**
     * Executes a drag-and-drop operation with configurable timing parameters.
     * <p>
     * This method performs a complete drag-and-drop sequence from one location to
     * another, with precise timing control at each stage. The timing parameters
     * from {@link ActionOptions} are applied to Sikuli's global settings before
     * the drag operation begins.
     * <p>
     * The method reports the drag coordinates at HIGH output level for debugging.
     * In mock mode, the actual drag is simulated by the mock provider.
     * <p>
     * The drag sequence consists of:
     * <ol>
     * <li>Pause before mouse down (UI settling time)</li>
     * <li>Mouse down at source location</li>
     * <li>Pause after mouse down (drag recognition time)</li>
     * <li>Move mouse to destination (with controlled speed)</li>
     * <li>Pause before mouse up (drop target activation)</li>
     * <li>Mouse up at destination</li>
     * <li>Pause after mouse up (post-action delay)</li>
     * </ol>
     * 
     * @param from The starting location for the drag. Must not be null.
     * @param to The destination location for the drag. Must not be null.
     * @param actionOptions Configuration containing all timing parameters for the drag stages.
     * @return {@code true} if the drag completed successfully (or was mocked),
     *         {@code false} if the drag operation failed.
     *         
     * @implNote This method modifies global Sikuli settings. While these are set before
     *           each drag, be aware that they affect all Sikuli operations until changed.
     */
    public boolean drag(Location from, Location to, ActionOptions actionOptions) {
        ConsoleReporter.format(ConsoleReporter.OutputLevel.HIGH, "drag %d.%d to %d.%d ",
                from.getCalculatedX(), from.getCalculatedY(), to.getCalculatedX(), to.getCalculatedY());
        if (FrameworkSettings.mock) return mock.drag();
        Settings.DelayBeforeMouseDown = actionOptions.getPauseBeforeMouseDown();
        Settings.DelayBeforeDrag = actionOptions.getPauseAfterMouseDown();
        Settings.MoveMouseDelay = actionOptions.getMoveMouseDelay();
        Settings.DelayBeforeDrop = actionOptions.getPauseBeforeMouseUp();
        if (!drag(from, to)) return false;
        time.wait(actionOptions.getPauseAfterMouseUp());
        return true;
    }
}
