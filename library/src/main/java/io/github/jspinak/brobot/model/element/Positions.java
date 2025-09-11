package io.github.jspinak.brobot.model.element;

import static io.github.jspinak.brobot.model.element.Positions.Name.*;

import java.util.HashMap;
import java.util.Map;

import io.github.jspinak.brobot.util.common.Pair;

/**
 * Defines standard relative positions within a rectangular area.
 *
 * <p>Positions provides a standardized way to reference common locations within any rectangular
 * region using semantic names instead of numeric coordinates. This abstraction is fundamental to
 * Brobot's approach of making automation scripts more readable and maintainable by using
 * human-understandable position references.
 *
 * <p>Position mapping:
 *
 * <ul>
 *   <li><b>TOPLEFT</b>: (0.0, 0.0) - Upper left corner
 *   <li><b>TOPMIDDLE</b>: (0.5, 0.0) - Center of top edge
 *   <li><b>TOPRIGHT</b>: (1.0, 0.0) - Upper right corner
 *   <li><b>MIDDLELEFT</b>: (0.0, 0.5) - Center of left edge
 *   <li><b>MIDDLEMIDDLE</b>: (0.5, 0.5) - Center of region
 *   <li><b>MIDDLERIGHT</b>: (1.0, 0.5) - Center of right edge
 *   <li><b>BOTTOMLEFT</b>: (0.0, 1.0) - Lower left corner
 *   <li><b>BOTTOMMIDDLE</b>: (0.5, 1.0) - Center of bottom edge
 *   <li><b>BOTTOMRIGHT</b>: (1.0, 1.0) - Lower right corner
 * </ul>
 *
 * <p>Key features:
 *
 * <ul>
 *   <li><b>Relative Coordinates</b>: All positions use 0.0-1.0 scale for universal applicability
 *   <li><b>Nine-point Grid</b>: Covers the most commonly needed reference points
 *   <li><b>Immutable Mapping</b>: Position definitions are fixed and thread-safe
 *   <li><b>Type Safety</b>: Enum-based names prevent invalid position references
 * </ul>
 *
 * <p>Use cases:
 *
 * <ul>
 *   <li>Clicking on corners or edges of UI elements
 *   <li>Positioning tooltips or overlays relative to components
 *   <li>Defining anchor points for drag operations
 *   <li>Specifying target locations within variable-sized regions
 * </ul>
 *
 * <p>In the model-based approach, Positions enables location specifications that adapt
 * automatically to different screen resolutions and element sizes. This flexibility is crucial for
 * creating robust automation that works across different environments without hardcoded
 * coordinates.
 *
 * @since 1.0
 * @see Location
 * @see Region
 * @see Position
 */
public class Positions {

    public enum Name {
        TOPLEFT,
        TOPMIDDLE,
        TOPRIGHT,
        MIDDLELEFT,
        MIDDLEMIDDLE,
        MIDDLERIGHT,
        BOTTOMLEFT,
        BOTTOMMIDDLE,
        BOTTOMRIGHT
    }

    private static final Map<Positions.Name, Pair<Double, Double>> positions = new HashMap<>();

    static {
        positions.put(TOPLEFT, Pair.of(0.0, 0.0));
        positions.put(TOPMIDDLE, Pair.of(.5, 0.0));
        positions.put(TOPRIGHT, Pair.of(1.0, 0.0));
        positions.put(MIDDLELEFT, Pair.of(0.0, .5));
        positions.put(MIDDLEMIDDLE, Pair.of(.5, .5));
        positions.put(MIDDLERIGHT, Pair.of(1.0, .5));
        positions.put(BOTTOMLEFT, Pair.of(0.0, 1.0));
        positions.put(BOTTOMMIDDLE, Pair.of(.5, 1.0));
        positions.put(BOTTOMRIGHT, Pair.of(1.0, 1.0));
    }

    public static Pair<Double, Double> getCoordinates(Positions.Name position) {
        return positions.get(position);
    }
}
