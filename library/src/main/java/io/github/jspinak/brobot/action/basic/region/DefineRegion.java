package io.github.jspinak.brobot.action.basic.region;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;

/**
 * Central orchestrator for region definition operations in the Brobot framework.
 *
 * <p>This class serves as the main entry point for the region definition functionality,
 * implementing the Strategy pattern to delegate to specific definition strategies based on the
 * {@link DefineRegionOptions.DefineAs} configuration. It maps each DefineAs option to its
 * corresponding implementation class, allowing flexible region definition approaches.
 *
 * <p>The class supports multiple region definition strategies:
 *
 * <ul>
 *   <li>{@link DefineRegionOptions.DefineAs#FOCUSED_WINDOW} - Defines region as the active window
 *       bounds
 *   <li>{@link DefineRegionOptions.DefineAs#MATCH} - Defines region as the match bounds
 *   <li>{@link DefineRegionOptions.DefineAs#BELOW_MATCH} - Defines region below a match
 *   <li>{@link DefineRegionOptions.DefineAs#ABOVE_MATCH} - Defines region above a match
 *   <li>{@link DefineRegionOptions.DefineAs#LEFT_OF_MATCH} - Defines region to the left of a match
 *   <li>{@link DefineRegionOptions.DefineAs#RIGHT_OF_MATCH} - Defines region to the right of a
 *       match
 *   <li>{@link DefineRegionOptions.DefineAs#INSIDE_ANCHORS} - Defines smallest region containing
 *       all anchors
 *   <li>{@link DefineRegionOptions.DefineAs#OUTSIDE_ANCHORS} - Defines largest region containing
 *       all anchors
 *   <li>{@link DefineRegionOptions.DefineAs#INCLUDING_MATCHES} - Defines region including all
 *       matches
 * </ul>
 *
 * @see DefineWithWindow
 * @see DefineWithMatch
 * @see DefineInsideAnchors
 * @see DefineOutsideAnchors
 * @see DefineIncludingMatches
 * @see DefineRegionOptions.DefineAs
 */
@Component
public class DefineRegion implements ActionInterface {

    @Override
    public Type getActionType() {
        return Type.DEFINE;
    }

    private final Map<DefineRegionOptions.DefineAs, ActionInterface> actions = new HashMap<>();

    /**
     * Constructs a DefineRegion instance with all available definition strategies.
     *
     * <p>This constructor uses dependency injection to receive all the specific definition
     * implementations and maps them to their corresponding DefineAs options. Note that multiple
     * DefineAs options may map to the same implementation (e.g., MATCH, BELOW_MATCH, ABOVE_MATCH
     * all use DefineWithMatch).
     *
     * @param defineWithWindow Strategy for defining regions based on window bounds
     * @param defineWithMatch Strategy for defining regions relative to matches
     * @param defineInsideAnchors Strategy for finding smallest region containing anchors
     * @param defineOutsideAnchors Strategy for finding largest region containing anchors
     * @param defineIncludingMatches Strategy for defining region that includes all matches
     */
    public DefineRegion(
            DefineWithWindow defineWithWindow,
            DefineWithMatch defineWithMatch,
            DefineInsideAnchors defineInsideAnchors,
            DefineOutsideAnchors defineOutsideAnchors,
            DefineIncludingMatches defineIncludingMatches) {
        actions.put(DefineRegionOptions.DefineAs.FOCUSED_WINDOW, defineWithWindow);
        actions.put(DefineRegionOptions.DefineAs.MATCH, defineWithMatch);
        actions.put(DefineRegionOptions.DefineAs.BELOW_MATCH, defineWithMatch);
        actions.put(DefineRegionOptions.DefineAs.ABOVE_MATCH, defineWithMatch);
        actions.put(DefineRegionOptions.DefineAs.LEFT_OF_MATCH, defineWithMatch);
        actions.put(DefineRegionOptions.DefineAs.RIGHT_OF_MATCH, defineWithMatch);
        actions.put(DefineRegionOptions.DefineAs.INSIDE_ANCHORS, defineInsideAnchors);
        actions.put(DefineRegionOptions.DefineAs.OUTSIDE_ANCHORS, defineOutsideAnchors);
        actions.put(DefineRegionOptions.DefineAs.INCLUDING_MATCHES, defineIncludingMatches);
    }

    /**
     * Delegates region definition to the appropriate strategy based on DefineRegionOptions.
     *
     * <p>This method extracts the DefineAs option from the DefineRegionOptions within the provided
     * ActionResult and delegates to the corresponding implementation. The actual region definition
     * is performed by the delegated class, which will add the defined region to the ActionResult.
     *
     * <p>Note: This method prints the DefineAs option to stdout for debugging purposes. In
     * production code, consider using a proper logging framework instead.
     *
     * @param matches The ActionResult containing the DefineRegionOptions that specify the
     *     definition strategy. The delegated implementation will add the defined region to this
     *     object.
     * @param objectCollections The collections of objects to use in the definition process. Their
     *     usage depends on the specific definition strategy.
     */
    @Override
    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        // Get the configuration - expecting DefineRegionOptions
        ActionConfig config = matches.getActionConfig();
        if (!(config instanceof DefineRegionOptions)) {
            throw new IllegalArgumentException(
                    "DefineRegion requires DefineRegionOptions configuration");
        }
        DefineRegionOptions defineOptions = (DefineRegionOptions) config;

        DefineRegionOptions.DefineAs defineAs = defineOptions.getDefineAs();
        System.out.print("Define as: " + defineAs + "| ");
        System.out.flush(); // Ensure output is flushed before proceeding

        ActionInterface action = actions.get(defineAs);
        if (action != null) {
            action.perform(matches, objectCollections);
        } else {
            throw new IllegalArgumentException("No action registered for DefineAs: " + defineAs);
        }
    }
}
