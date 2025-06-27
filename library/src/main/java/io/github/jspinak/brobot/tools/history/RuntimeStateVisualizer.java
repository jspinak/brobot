package io.github.jspinak.brobot.tools.history;

import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.tools.history.draw.DrawRect;
import io.github.jspinak.brobot.tools.history.visual.StateVisualization;
import io.github.jspinak.brobot.util.image.constants.BgrColorConstants;
import io.github.jspinak.brobot.model.state.State;

import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Visualizes Brobot states with color-coded elements and boundaries.
 * <p>
 * This component creates visual representations of states by overlaying colored
 * rectangles on screenshots to indicate state boundaries and element locations.
 * It uses actual match snapshots to show where elements were found, providing
 * a runtime view of state recognition results.
 * <p>
 * Color coding scheme:
 * <ul>
 * <li>Blue: State boundaries defining the active region</li>
 * <li>Red: Regular state elements (images, buttons, etc.)</li>
 * <li>Green: Transition elements (reserved for future implementation)</li>
 * </ul>
 * <p>
 * Key differences from {@link StateLayoutVisualizer}:
 * <ul>
 * <li>Uses match snapshots showing actual found positions</li>
 * <li>Works with runtime data rather than state definitions</li>
 * <li>Shows what was actually detected vs. what was expected</li>
 * <li>Includes state boundary visualization</li>
 * </ul>
 * <p>
 * Future enhancements:
 * <ul>
 * <li>Transition element detection and green highlighting</li>
 * <li>Multiple match visualization for elements found multiple times</li>
 * <li>Confidence score visualization through color intensity</li>
 * <li>Animation of state changes over time</li>
 * </ul>
 * <p>
 * Thread safety: Stateless component safe for concurrent use.
 *
 * @see StateVisualization
 * @see ActionRecord
 * @see DrawRect
 * @see State
 */
@Component
public class RuntimeStateVisualizer {

    private final DrawRect drawRect;

    public RuntimeStateVisualizer(DrawRect drawRect) {
        this.drawRect = drawRect;
    }

    /**
     * Creates a new state illustration from a screenshot.
     * <p>
     * Convenience method that initializes a StateIllustration with the provided
     * screenshot and delegates to the main drawing method. This allows callers
     * to provide just a state and screenshot without managing the illustration
     * object lifecycle.
     *
     * @param state the state to visualize with its boundaries and elements
     * @param screenshot the background image showing the application UI
     * @return completed StateIllustration with visual overlays applied
     */
    public StateVisualization drawState(State state, Image screenshot) {
        StateVisualization stateIllustration = new StateVisualization(screenshot);
        return drawState(state, stateIllustration);
    }

    /**
     * Draws state boundaries and element locations on an existing illustration.
     * <p>
     * Creates a visual representation of the state by:
     * <ol>
     * <li>Cloning the screenshot to preserve the original</li>
     * <li>Drawing blue rectangles around state boundaries</li>
     * <li>Iterating through all state images to find match locations</li>
     * <li>Drawing red rectangles around found elements</li>
     * <li>Updating the illustration with the annotated image</li>
     * </ol>
     * <p>
     * Current limitations:
     * <ul>
     * <li>Only shows the first match when multiple matches exist</li>
     * <li>Transition detection is not yet implemented (placeholder exists)</li>
     * <li>No visualization of match confidence or similarity scores</li>
     * </ul>
     * <p>
     * The method modifies the provided StateIllustration object by setting
     * the illustrated screenshot, following a builder-like pattern.
     *
     * @param state source of boundaries and element definitions
     * @param stateIllustration container for the screenshot and illustration; modified in place
     * @return the same StateIllustration object with illustration applied
     */
    public StateVisualization drawState(State state, StateVisualization stateIllustration) {
        Mat illustration = stateIllustration.getScreenshotAsMat().clone();
        drawRect.drawRectAroundRegion(illustration, state.getBoundaries(), BgrColorConstants.BLUE.getScalar());
        Set<StateImage> sios = state.getStateImages();
        for (StateImage stateImage : sios) {
            boolean isTransition = false; //need methods to determine which images are involved with transitions
            List<ActionRecord> snapshots = stateImage.getAllMatchSnapshots();
            if (!snapshots.isEmpty()) {
                if (isTransition) drawRect.drawRectAroundMatch(
                        illustration, snapshots.get(0).getMatchList().get(0), BgrColorConstants.GREEN.getScalar());
                else drawRect.drawRectAroundMatch(
                        illustration, snapshots.get(0).getMatchList().get(0), BgrColorConstants.RED.getScalar());
            }
        }
        stateIllustration.setIllustratedScreenshot(illustration);
        return stateIllustration;
    }

}
