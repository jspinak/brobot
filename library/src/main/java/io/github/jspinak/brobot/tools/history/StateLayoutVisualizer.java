package io.github.jspinak.brobot.tools.history;

import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.util.image.io.ImageFileUtilities;
import io.github.jspinak.brobot.util.image.visualization.MatBuilder;

/**
 * Creates visual representations of Brobot states showing expected element positions.
 *
 * <p>This component generates state illustrations by compositing state images at their expected
 * screen locations. It provides a visual map of where Brobot expects to find UI elements, useful
 * for debugging state definitions and understanding application structure.
 *
 * <p>Illustration approach:
 *
 * <ul>
 *   <li>Uses the first pattern from each StateImage as the representative
 *   <li>Places patterns at their fixed search region locations
 *   <li>Composites all patterns into a single visualization
 *   <li>Creates a spatial representation of the state's expected layout
 * </ul>
 *
 * <p>Use cases:
 *
 * <ul>
 *   <li>Debugging state definitions to verify element positions
 *   <li>Documenting expected UI layouts for different application states
 *   <li>Visualizing state transitions by comparing illustrations
 *   <li>Training and onboarding to understand state structures
 * </ul>
 *
 * <p>Limitations:
 *
 * <ul>
 *   <li>Only shows fixed regions, not dynamic search areas
 *   <li>Uses first pattern only when multiple patterns exist
 *   <li>Requires patterns to have defined regions
 *   <li>May have overlapping images if regions overlap
 * </ul>
 *
 * <p>Alternative approaches mentioned include using MatchSnapshots for actual found positions
 * rather than expected positions.
 *
 * @see State
 * @see StateImage
 * @see Pattern
 * @see MatBuilder
 */
@Component
public class StateLayoutVisualizer {

    private final ImageFileUtilities imageUtils;

    public StateLayoutVisualizer(ImageFileUtilities imageUtils) {
        this.imageUtils = imageUtils;
    }

    /**
     * Creates a composite visualization of a state's expected element layout.
     *
     * <p>Builds a visual map by placing each StateImage's first pattern at its defined search
     * region location. This shows where Brobot expects to find UI elements when in this state,
     * creating a spatial representation of the state's structure.
     *
     * <p>Processing steps:
     *
     * <ol>
     *   <li>Initialize a blank canvas with MatBuilder
     *   <li>Iterate through all StateImages in the state
     *   <li>For each non-empty StateImage, extract the first pattern
     *   <li>If the pattern has a defined region, place it at that location
     *   <li>Build the final composite image
     * </ol>
     *
     * <p>Patterns without defined regions are skipped, as they have no fixed position to display.
     * The resulting illustration may have empty areas where patterns are undefined or regions don't
     * contain patterns.
     *
     * @param state the state containing images and their expected positions
     * @return composite Mat showing all patterns at their expected locations
     */
    public Mat illustrateWithFixedSearchRegions(State state) {
        MatBuilder matBuilder = new MatBuilder().init();
        for (StateImage image : state.getStateImages()) {
            if (!image.isEmpty()) {
                Pattern p = image.getPatterns().get(0);
                if (p.isDefined()) {
                    Region r = p.getRegion();
                    Location xy = new Location(r.x(), r.y());
                    Mat mat = p.getMat();
                    matBuilder.addSubMat(xy, mat);
                }
            }
        }
        return matBuilder.build();
    }

    /**
     * Generates and saves a state illustration to a file.
     *
     * <p>Convenience method that combines illustration generation and file writing. The
     * illustration shows all state elements at their expected positions, saved with a unique
     * filename to prevent overwrites.
     *
     * <p>The unique filename generation ensures multiple illustrations can be saved without
     * conflicts, useful for comparing states over time or across different application versions.
     *
     * @param state the state to illustrate and save
     * @param filename base filename for the illustration (will be made unique)
     */
    public void writeIllustratedStateToFile(State state, String filename) {
        Mat mat = illustrateWithFixedSearchRegions(state);
        imageUtils.writeWithUniqueFilename(mat, filename);
    }
}
