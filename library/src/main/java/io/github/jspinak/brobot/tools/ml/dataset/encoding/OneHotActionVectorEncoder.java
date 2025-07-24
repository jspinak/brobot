package io.github.jspinak.brobot.tools.ml.dataset.encoding;

import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.tools.ml.dataset.model.ActionVector;

import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * One-hot encoding implementation for converting actions to vectors.
 * <p>
 * This implementation uses one-hot encoding for action types to treat them as distinct
 * categories rather than ordinal values. This prevents the neural network from inferring
 * relationships between action types (e.g., that DRAG is somehow "between" CLICK and TYPE).
 * <p>
 * <strong>Vector structure:</strong>
 * <ul>
 * <li>Positions 0-5: One-hot encoded action type (CLICK, DRAG, TYPE, MOVE, SCROLL, HIGHLIGHT)</li>
 * <li>Positions 6-9: Coordinates (x, y, width, height) from the best match</li>
 * <li>Positions 10-11: Highlight-specific options (color index, all-at-once flag)</li>
 * </ul>
 * <p>
 * <strong>Design decisions:</strong>
 * <ul>
 * <li>Only includes basic GUI-modifying actions (excludes FIND, VANISH, GET_TEXT, etc.)</li>
 * <li>HIGHLIGHT is included as a simple test case for neural network training</li>
 * <li>Failed actions (empty matches) return an empty vector</li>
 * <li>Assumes FIND operations are handled separately or by another neural network</li>
 * </ul>
 * <p>
 * The implementation extracts coordinates from successful matches after FIND operations
 * have been performed, making the vectors suitable for training networks that predict
 * direct GUI interactions.
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

    /**
     * Enum representing highlight colors with their encoded values.
     */
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
        ActionOptions actOpt = matches.getActionOptions();
        setCoordinates(vec, matches);
        setAction(vec, actOpt);
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
        if (matches.getActionOptions().getAction() != ActionOptions.Action.HIGHLIGHT) return;
        String color = matches.getActionOptions().getHighlightColor();
        HighlightColor highlightColor = HighlightColor.fromString(color);
        if (highlightColor != null) {
            vec[HIGHLIGHT_COLOR_POS] = (short) highlightColor.getEncodedValue();
        }
        if (matches.getActionOptions().isHighlightAllAtOnce()) {
            vec[HIGHLIGHT_ALL_AT_ONCE_POS] = 1;
        }
    }

    private void setAction(short[] vec, ActionOptions actionOptions) {
        switch (actionOptions.getAction()) {
            case CLICK -> vec[CLICK_POS] = 1;
            case DRAG -> vec[DRAG_POS] = 1;
            case TYPE -> vec[TYPE_POS] = 1;
            case MOVE -> vec[MOVE_POS] = 1;
            case SCROLL_MOUSE_WHEEL -> vec[SCROLL_POS] = 1; // ScrollDirection UP DOWN
            case HIGHLIGHT -> vec[HIGHLIGHT_POS] = 1;
            /*case MOUSE_DOWN -> vec[6] = 1;
            case MOUSE_UP -> vec[7] = 1;
            case KEY_DOWN -> vec[8] = 1;
            case KEY_UP -> vec[9] = 1;
            case CLICK_UNTIL -> vec[10] = 1;
            case FIND: vec[] = 1; break;
            case DEFINE: vec[] = 1; break;
            case VANISH: vec[] = 1; break;
            case GET_TEXT: vec[] = 1; break;
            case CLASSIFY: vec[] = 1; break;*/
        }
    }

    public ActionOptions toActionOptions(ActionVector actionVector) {
        ActionOptions actionOptions = new ActionOptions();
        short[] vec = actionVector.getVector();
        setAction(actionOptions, vec);
        //... TODO
        return actionOptions;
    }

    /**
     * Extracts object data from an ActionVector to create an ObjectCollection.
     * <p>
     * This method would reconstruct the ObjectCollection (containing regions, locations,
     * or other objects) from the vector representation. Currently unimplemented.
     * <p>
     * TODO: Implement extraction of coordinates and construction of appropriate objects
     * based on the action type encoded in the vector.
     *
     * @param actionVector The vector containing encoded action and coordinate data
     * @return An ObjectCollection reconstructed from the vector data, or null if unimplemented
     */
    public ObjectCollection toObjectCollection(ActionVector actionVector) {
        return null;
    }
    
    private void setAction(ActionOptions actionOptions, short[] vec) {
        if (vec[CLICK_POS] == 1) actionOptions.setAction(ActionOptions.Action.CLICK);
        else if (vec[DRAG_POS] == 1) actionOptions.setAction(ActionOptions.Action.DRAG);
        else if (vec[TYPE_POS] == 1) actionOptions.setAction(ActionOptions.Action.TYPE);
        else if (vec[MOVE_POS] == 1) actionOptions.setAction(ActionOptions.Action.MOVE);
        else if (vec[SCROLL_POS] == 1) actionOptions.setAction(ActionOptions.Action.SCROLL_MOUSE_WHEEL);
        else if (vec[HIGHLIGHT_POS] == 1) actionOptions.setAction(ActionOptions.Action.HIGHLIGHT);
    }

}