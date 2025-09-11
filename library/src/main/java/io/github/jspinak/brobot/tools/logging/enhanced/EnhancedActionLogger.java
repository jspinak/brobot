package io.github.jspinak.brobot.tools.logging.enhanced;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.tools.logging.ActionLogger;
import io.github.jspinak.brobot.tools.logging.visual.VisualFeedbackOptions;

/**
 * Enhanced version of ActionLogger that adds visual feedback capabilities and improved console
 * reporting for action execution.
 *
 * <p>This interface extends the standard ActionLogger with methods that support visual
 * highlighting, GUI access problem reporting, and detailed image search logging.
 *
 * @see ActionLogger for the base logging interface
 * @see VisualFeedbackOptions for visual feedback configuration
 */
public interface EnhancedActionLogger extends ActionLogger {

    /**
     * Logs an action with visual feedback options.
     *
     * @param action The action type (e.g., "FIND", "CLICK", "TYPE")
     * @param target The target objects for the action
     * @param result The result of the action execution
     * @param visualOptions Visual feedback configuration
     */
    void logActionWithVisuals(
            String action,
            ObjectCollection target,
            ActionResult result,
            VisualFeedbackOptions visualOptions);

    /**
     * Logs GUI access problems that prevent automation from working.
     *
     * @param problem Description of the GUI access problem
     * @param error Optional exception that caused the problem
     */
    void logGuiAccessProblem(String problem, Exception error);

    /**
     * Logs detailed image search information.
     *
     * @param imageName Name of the image being searched for
     * @param region The region being searched (null for full screen)
     * @param found Whether the image was found
     */
    void logImageSearch(String imageName, Region region, boolean found);

    /**
     * Logs the start of an image search operation.
     *
     * @param target The state object being searched for
     * @param searchRegions The regions that will be searched
     */
    void logSearchStart(StateObject target, Region... searchRegions);

    /**
     * Logs the completion of an image search operation.
     *
     * @param target The state object that was searched for
     * @param result The search result
     * @param duration Time taken for the search in milliseconds
     */
    void logSearchComplete(StateObject target, ActionResult result, long duration);

    /**
     * Logs a click action with visual feedback at the click location.
     *
     * @param x X coordinate of the click
     * @param y Y coordinate of the click
     * @param success Whether the click was successful
     */
    void logClickWithVisual(int x, int y, boolean success);

    /**
     * Logs a drag action with visual feedback showing the drag path.
     *
     * @param fromX Starting X coordinate
     * @param fromY Starting Y coordinate
     * @param toX Ending X coordinate
     * @param toY Ending Y coordinate
     * @param success Whether the drag was successful
     */
    void logDragWithVisual(int fromX, int fromY, int toX, int toY, boolean success);

    /**
     * Logs when highlighting is performed.
     *
     * @param region The region being highlighted
     * @param color The highlight color
     * @param duration Duration of the highlight in seconds
     */
    void logHighlight(Region region, String color, double duration);

    /**
     * Enables or disables visual feedback globally.
     *
     * @param enabled true to enable visual feedback, false to disable
     */
    void setVisualFeedbackEnabled(boolean enabled);

    /**
     * Checks if visual feedback is currently enabled.
     *
     * @return true if visual feedback is enabled
     */
    boolean isVisualFeedbackEnabled();

    /**
     * Sets the verbosity level for console output.
     *
     * @param level The verbosity level ("QUIET", "NORMAL", "VERBOSE")
     */
    void setConsoleVerbosity(String level);

    /**
     * Performs a GUI access check and logs any problems found.
     *
     * @return true if GUI is accessible, false otherwise
     */
    boolean checkAndLogGuiAccess();
}
