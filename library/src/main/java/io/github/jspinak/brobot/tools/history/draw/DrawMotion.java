package io.github.jspinak.brobot.tools.history.draw;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.tools.history.HistoryFileNamer;

/**
 * Handles the visualization and saving of motion detection masks from action results.
 *
 * <p>This class is responsible for persisting motion detection results as image files, typically
 * showing areas of movement or change between frames. Motion masks are binary or grayscale images
 * where pixel values indicate the presence and intensity of detected motion.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Saves motion masks from ActionResult objects
 *   <li>Generates standardized filenames for motion visualizations
 *   <li>Integrates with the Brobot history and visualization system
 * </ul>
 *
 * <p>Motion masks are useful for:
 *
 * <ul>
 *   <li>Debugging motion-based find operations
 *   <li>Visualizing areas of change in the application
 *   <li>Analyzing animation and transition effects
 *   <li>Detecting dynamic UI elements
 * </ul>
 *
 * <p>The saved motion masks can be used to:
 *
 * <ul>
 *   <li>Review what areas triggered motion detection
 *   <li>Tune motion sensitivity parameters
 *   <li>Create documentation of dynamic behaviors
 *   <li>Debug false positive/negative motion detections
 * </ul>
 *
 * @see ActionResult#getMask()
 * @see HistoryFileNamer
 * @see ActionType#FIND
 */
@Component
public class DrawMotion {

    private HistoryFileNamer historyFileNamer;

    public DrawMotion(HistoryFileNamer historyFileNamer) {
        this.historyFileNamer = historyFileNamer;
    }

    /**
     * Saves the motion detection mask from an action result to a file.
     *
     * <p>This method extracts the motion mask from the ActionResult and saves it as an image file.
     * The filename is generated using a standardized naming convention that includes the action
     * type (FIND) and a "motion" suffix.
     *
     * <p>The motion mask is typically:
     *
     * <ul>
     *   <li>A grayscale image where brighter pixels indicate more motion
     *   <li>Binary (black/white) for simple motion presence detection
     *   <li>Multi-level grayscale for motion intensity visualization
     * </ul>
     *
     * <p>File naming example: "find_motion_timestamp.png"
     *
     * @param matches The ActionResult containing the motion mask to save. Must not be null and must
     *     contain a valid mask.
     * @throws RuntimeException if the mask is null or image writing fails
     */
    public void draw(ActionResult matches) {
        String outputPath = historyFileNamer.getFilename(ActionType.FIND, "motion");
        imwrite(outputPath, matches.getMask());
    }
}
