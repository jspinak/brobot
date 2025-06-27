package io.github.jspinak.brobot.action.basic.visual;


import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Central orchestrator for region definition operations in the Brobot framework.
 * 
 * <p>This class serves as the main entry point for the region definition functionality,
 * implementing the Strategy pattern to delegate to specific definition strategies based on
 * the {@link ActionOptions.DefineAs} configuration. It maps each DefineAs option to its
 * corresponding implementation class, allowing flexible region definition approaches.</p>
 * 
 * <p>The class supports multiple region definition strategies:
 * <ul>
 *   <li>{@link ActionOptions.DefineAs#FOCUSED_WINDOW} - Defines region as the active window bounds</li>
 *   <li>{@link ActionOptions.DefineAs#MATCH} - Defines region as the match bounds</li>
 *   <li>{@link ActionOptions.DefineAs#BELOW_MATCH} - Defines region below a match</li>
 *   <li>{@link ActionOptions.DefineAs#ABOVE_MATCH} - Defines region above a match</li>
 *   <li>{@link ActionOptions.DefineAs#LEFT_OF_MATCH} - Defines region to the left of a match</li>
 *   <li>{@link ActionOptions.DefineAs#RIGHT_OF_MATCH} - Defines region to the right of a match</li>
 *   <li>{@link ActionOptions.DefineAs#INSIDE_ANCHORS} - Defines smallest region containing all anchors</li>
 *   <li>{@link ActionOptions.DefineAs#OUTSIDE_ANCHORS} - Defines largest region containing all anchors</li>
 *   <li>{@link ActionOptions.DefineAs#INCLUDING_MATCHES} - Defines region including all matches</li>
 * </ul>
 * </p>
 * 
 * @see DefineWithWindow
 * @see DefineWithMatch
 * @see DefineInsideAnchors
 * @see DefineOutsideAnchors
 * @see DefineIncludingMatches
 * @see ActionOptions.DefineAs
 */
@Component
public class DefineRegion implements ActionInterface {

    private final Map<ActionOptions.DefineAs, ActionInterface> actions = new HashMap<>();

    /**
     * Constructs a DefineRegion instance with all available definition strategies.
     * 
     * <p>This constructor uses dependency injection to receive all the specific definition
     * implementations and maps them to their corresponding DefineAs options. Note that
     * multiple DefineAs options may map to the same implementation (e.g., MATCH, BELOW_MATCH,
     * ABOVE_MATCH all use DefineWithMatch).</p>
     * 
     * @param defineWithWindow Strategy for defining regions based on window bounds
     * @param defineWithMatch Strategy for defining regions relative to matches
     * @param defineInsideAnchors Strategy for finding smallest region containing anchors
     * @param defineOutsideAnchors Strategy for finding largest region containing anchors
     * @param defineIncludingMatches Strategy for defining region that includes all matches
     */
    public DefineRegion(DefineWithWindow defineWithWindow, DefineWithMatch defineWithMatch,
                        DefineInsideAnchors defineInsideAnchors, DefineOutsideAnchors defineOutsideAnchors,
                        DefineIncludingMatches defineIncludingMatches) {
        actions.put(ActionOptions.DefineAs.FOCUSED_WINDOW, defineWithWindow);
        actions.put(ActionOptions.DefineAs.MATCH, defineWithMatch);
        actions.put(ActionOptions.DefineAs.BELOW_MATCH, defineWithMatch);
        actions.put(ActionOptions.DefineAs.ABOVE_MATCH, defineWithMatch);
        actions.put(ActionOptions.DefineAs.LEFT_OF_MATCH, defineWithMatch);
        actions.put(ActionOptions.DefineAs.RIGHT_OF_MATCH, defineWithMatch);
        actions.put(ActionOptions.DefineAs.INSIDE_ANCHORS, defineInsideAnchors);
        actions.put(ActionOptions.DefineAs.OUTSIDE_ANCHORS, defineOutsideAnchors);
        actions.put(ActionOptions.DefineAs.INCLUDING_MATCHES, defineIncludingMatches);
    }

    /**
     * Delegates region definition to the appropriate strategy based on ActionOptions.
     * 
     * <p>This method extracts the DefineAs option from the ActionOptions within the
     * provided ActionResult and delegates to the corresponding implementation. The
     * actual region definition is performed by the delegated class, which will add
     * the defined region to the ActionResult.</p>
     * 
     * <p>Note: This method prints the DefineAs option to stdout for debugging purposes.
     * In production code, consider using a proper logging framework instead.</p>
     * 
     * @param matches The ActionResult containing the ActionOptions that specify the
     *                definition strategy. The delegated implementation will add the
     *                defined region to this object.
     * @param objectCollections The collections of objects to use in the definition process.
     *                          Their usage depends on the specific definition strategy.
     */
    @Override
    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        System.out.print("Define as: " + actionOptions.getDefineAs()+"| ");
        actions.get(actionOptions.getDefineAs()).perform(matches, objectCollections);
    }

}
