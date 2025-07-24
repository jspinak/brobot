package io.github.jspinak.brobot.action.composite.drag;

import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import org.springframework.stereotype.Component;

/**
 * Factory class for creating specialized ActionOptions configurations for drag operations.
 * <p>
 * This utility class decomposes complex drag operations into their constituent parts,
 * creating properly configured ActionOptions for each phase of a drag gesture. It ensures
 * that timing and positioning parameters from the original drag request are correctly
 * distributed to the individual mouse actions that comprise the drag operation.
 * 
 * <p>A typical drag operation consists of:</p>
 * <ol>
 *   <li>Finding the source location (with offsets applied)</li>
 *   <li>Mouse down at the source</li>
 *   <li>Finding the destination location (with drag-specific offsets)</li>
 *   <li>Mouse move to the destination</li>
 *   <li>Mouse up at the destination</li>
 * </ol>
 * 
 * <p>This class extracts the relevant parameters from a master ActionOptions object
 * and creates specialized configurations for each phase, ensuring proper timing
 * synchronization and offset application throughout the drag sequence.</p>
 * 
 * @see ActionOptions
 * @see Drag
 * @see SimpleDrag
 * @see MultipleDrags
 * 
 * @deprecated Since version 2.0, use {@link DragOptions} instead which provides
 *             a cleaner API for configuring drag operations without needing to
 *             decompose into individual ActionOptions.
 */
@Deprecated(since = "1.1.0", forRemoval = true)
@Component
public class ActionOptionsForDrag {

    /**
     * Creates ActionOptions configured for the mouse down phase of a drag operation.
     * <p>
     * Extracts timing parameters specific to the mouse press action, including
     * delays before and after the button press. The click type (left, right, middle)
     * is preserved from the original options to support different drag behaviors.
     * 
     * @param actionOptions The master drag options containing all timing parameters
     * @return A new ActionOptions instance configured specifically for MOUSE_DOWN action
     *         with appropriate timing delays
     */
    public ActionOptions getMouseDown(ActionOptions actionOptions) {
        return new ActionOptions.Builder()
                .setAction(ActionOptions.Action.MOUSE_DOWN)
                .setPauseBeforeMouseDown(actionOptions.getPauseBeforeMouseDown())
                .setPauseAfterMouseDown(actionOptions.getPauseAfterMouseDown())
                .setClickType(actionOptions.getClickType())
                .build();
    }

    /**
     * Creates ActionOptions configured for the mouse up phase of a drag operation.
     * <p>
     * Extracts timing parameters for releasing the mouse button at the end of a drag.
     * The configured delays ensure smooth completion of the drag gesture and allow
     * for any UI animations or processing that might occur upon drop.
     * 
     * @param actionOptions The master drag options containing all timing parameters
     * @return A new ActionOptions instance configured specifically for MOUSE_UP action
     *         with appropriate timing delays
     */
    public ActionOptions getMouseUp(ActionOptions actionOptions) {
        return new ActionOptions.Builder()
                .setAction(ActionOptions.Action.MOUSE_UP)
                .setPauseBeforeMouseUp(actionOptions.getPauseBeforeMouseUp())
                .setPauseAfterMouseUp(actionOptions.getPauseAfterMouseUp())
                .build();
    }

    /**
     * Creates ActionOptions for finding the drag source location.
     * <p>
     * Configures a FIND action with the source-specific parameters, including
     * similarity threshold, search regions, and positional offsets. The addX and
     * addY values allow for clicking at an offset from the found pattern's center,
     * useful for grabbing specific parts of UI elements.
     * 
     * @param actionOptions The master drag options containing search and offset parameters
     * @return A new ActionOptions instance configured for finding the drag source
     *         with appropriate similarity, search regions, and position offsets
     */
    public ActionOptions getFindFrom(ActionOptions actionOptions) {
        return new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setMinSimilarity(actionOptions.getSimilarity())
                .setSearchRegions(actionOptions.getSearchRegions())
                .setAddX(actionOptions.getAddX())
                .setAddY(actionOptions.getAddY())
                .build();
    }

    /**
     * Creates ActionOptions for finding the drag destination location.
     * <p>
     * Similar to getFindFrom but uses drag-specific destination offsets. The
     * dragToOffsetX and dragToOffsetY values allow for dropping at a specific
     * position relative to the found pattern, enabling precise placement in
     * drop zones or alignment with UI constraints.
     * 
     * @param actionOptions The master drag options containing search parameters and
     *                      destination-specific offsets
     * @return A new ActionOptions instance configured for finding the drag destination
     *         with appropriate similarity, search regions, and destination offsets
     */
    public ActionOptions getFindTo(ActionOptions actionOptions) {
        return new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setMinSimilarity(actionOptions.getSimilarity())
                .setSearchRegions(actionOptions.getSearchRegions())
                .setAddX(actionOptions.getDragToOffsetX())
                .setAddY(actionOptions.getDragToOffsetY())
                .build();
    }

    /**
     * Creates ActionOptions for the mouse movement phase of a drag operation.
     * <p>
     * Configures the MOVE action that occurs while the mouse button is held down.
     * The moveMouseDelay parameter controls the speed of the cursor movement,
     * allowing for smooth, human-like drag gestures or fast, efficient movements
     * depending on application requirements.
     * 
     * @param actionOptions The master drag options containing movement timing parameters
     * @return A new ActionOptions instance configured specifically for MOVE action
     *         with appropriate movement delay settings
     */
    public ActionOptions getMove(ActionOptions actionOptions) {
        return new ActionOptions.Builder()
                .setAction(ActionOptions.Action.MOVE)
                .setMoveMouseDelay(actionOptions.getMoveMouseDelay())
                .build();
    }

}
