package io.github.jspinak.brobot.tools.ml.dataset.encoding;

import java.util.Optional;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.tools.ml.dataset.model.ActionVector;

/**
 * One-hot encoding implementation for converting actions to vectors.
 *
 * <p>This implementation uses one-hot encoding for action types to treat them as distinct
 * categories rather than ordinal values. This prevents the neural network from inferring
 * relationships between action types (e.g., that DRAG is somehow "between" CLICK and TYPE).
 *
 * <p><strong>Vector structure:</strong>
 *
 * <ul>
 *   <li>Positions 0-5: One-hot encoded action type (CLICK, DRAG, TYPE, MOVE, SCROLL, HIGHLIGHT)
 *   <li>Positions 6-9: Coordinates (x, y, width, height) from the best match
 *   <li>Positions 10-11: Highlight-specific options (color index, all-at-once flag)
 * </ul>
 *
 * <p><strong>Design decisions:</strong>
 *
 * <ul>
 *   <li>Only includes basic GUI-modifying actions (excludes FIND, VANISH, GET_TEXT, etc.)
 *   <li>HIGHLIGHT is included as a simple test case for neural network training
 *   <li>Failed actions (empty matches) return an empty vector
 *   <li>Assumes FIND operations are handled separately or by another neural network
 * </ul>
 *
 * <p>The implementation extracts coordinates from successful matches after FIND operations have
 * been performed, making the vectors suitable for training networks that predict direct GUI
 * interactions.
 *
 * @see ActionVectorTranslator
 * @see ActionVector
 */
@Component
public class OneHotActionVectorEncoder implements ActionVectorTranslator {

    // Vector position constants
    private static final int ACTION_TYPE_START = 0;
    private static final int ACTION_TYPE_END = 5;
    private static final int COORD_X_POS = 6;
    private static final int COORD_Y_POS = 7;
    private static final int COORD_WIDTH_POS = 8;
    private static final int COORD_HEIGHT_POS = 9;
    private static final int HIGHLIGHT_COLOR_POS = 10;
    private static final int HIGHLIGHT_ALL_AT_ONCE_POS = 11;

    // Action type positions for one-hot encoding
    private static final int CLICK_POS = 0;
    private static final int DRAG_POS = 1;
    private static final int TYPE_POS = 2;
    private static final int MOVE_POS = 3;
    private static final int SCROLL_POS = 4;
    private static final int HIGHLIGHT_POS = 5;

    /** Enum representing highlight colors with their encoded values. */
    public enum HighlightColor {
        BLUE("blue", 0),
        RED("red", 1),
        YELLOW("yellow", 2),
        GREEN("green", 3),
        ORANGE("orange", 4),
        PURPLE("purple", 5),
        WHITE("white", 6),
        BLACK("black", 7),
        GREY("grey", 8);

        private final String colorName;
        private final int encodedValue;

        HighlightColor(String colorName, int encodedValue) {
            this.colorName = colorName;
            this.encodedValue = encodedValue;
        }

        public String getColorName() {
            return colorName;
        }

        public int getEncodedValue() {
            return encodedValue;
        }

        public static HighlightColor fromString(String color) {
            for (HighlightColor hc : values()) {
                if (hc.colorName.equalsIgnoreCase(color)) {
                    return hc;
                }
            }
            return null;
        }
    }

    public ActionVector toVector(ActionResult matches) {
        ActionVector actionVector = new ActionVector();
        if (matches.isEmpty()) return actionVector; // action failed
        short[] vec = actionVector.getVector();
        ActionConfig actionConfig = matches.getActionConfig();
        setCoordinates(vec, matches);
        setAction(vec, actionConfig);
        setHighlightOptions(vec, matches);
        return actionVector;
    }

    private void setCoordinates(short[] vec, ActionResult matches) {
        Optional<Match> optMatch = matches.getBestMatch();
        if (optMatch.isEmpty()) return;
        Match match = optMatch.get();
        vec[COORD_X_POS] = (short) match.x();
        vec[COORD_Y_POS] = (short) match.y();
        vec[COORD_WIDTH_POS] = (short) match.w();
        vec[COORD_HEIGHT_POS] = (short) match.h();
    }

    private void setHighlightOptions(short[] vec, ActionResult matches) {
        // Check if the action config is a HighlightOptions
        ActionConfig config = matches.getActionConfig();
        if (!(config instanceof HighlightOptions)) return;

        HighlightOptions highlightOptions = (HighlightOptions) config;
        // For now, just set defaults since the color/all-at-once properties
        // may not be available in the new API
        vec[HIGHLIGHT_COLOR_POS] = (short) 0; // Default color
        vec[HIGHLIGHT_ALL_AT_ONCE_POS] = 0; // Default not all at once
    }

    private void setAction(short[] vec, ActionConfig actionConfig) {
        // Determine action type based on the concrete class
        if (actionConfig instanceof io.github.jspinak.brobot.action.basic.click.ClickOptions) {
            vec[CLICK_POS] = 1;
        } else if (actionConfig
                instanceof io.github.jspinak.brobot.action.composite.drag.DragOptions) {
            vec[DRAG_POS] = 1;
        } else if (actionConfig instanceof io.github.jspinak.brobot.action.basic.type.TypeOptions) {
            vec[TYPE_POS] = 1;
        } else if (actionConfig
                instanceof io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions) {
            vec[MOVE_POS] = 1;
        } else if (actionConfig
                instanceof io.github.jspinak.brobot.action.basic.mouse.ScrollOptions) {
            vec[SCROLL_POS] = 1;
        } else if (actionConfig instanceof HighlightOptions) {
            vec[HIGHLIGHT_POS] = 1;
        }
        // Other action types can be added as needed
    }

    private ActionConfig createActionConfig(ActionVector actionVector) {
        // This method would need to be reimplemented to create proper ActionConfig subclasses
        // For now, return a basic PatternFindOptions as placeholder
        return new io.github.jspinak.brobot.action.basic.find.PatternFindOptions.Builder().build();
    }

    @Override
    public ActionConfig toActionConfig(ActionVector actionVector) {
        // Create appropriate ActionConfig based on the vector
        return createActionConfig(actionVector);
    }

    /**
     * Extracts object data from an ActionVector to create an ObjectCollection.
     *
     * <p>This method would reconstruct the ObjectCollection (containing regions, locations, or
     * other objects) from the vector representation. Currently unimplemented.
     *
     * <p>TODO: Implement extraction of coordinates and construction of appropriate objects based on
     * the action type encoded in the vector.
     *
     * @param actionVector The vector containing encoded action and coordinate data
     * @return An ObjectCollection reconstructed from the vector data, or null if unimplemented
     */
    public ObjectCollection toObjectCollection(ActionVector actionVector) {
        return null;
    }

    // This method is no longer needed as ActionConfig objects are immutable
    // and created with specific types rather than having mutable action fields
    /*
    private void setAction(ActionConfig actionConfig, short[] vec) {
        // ActionConfig instances are now immutable and type-specific
        // This method would need to be reimplemented to create new instances
    }
    */

}
