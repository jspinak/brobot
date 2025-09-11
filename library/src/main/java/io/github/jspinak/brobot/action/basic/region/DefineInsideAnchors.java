package io.github.jspinak.brobot.action.basic.region;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.capture.AnchorRegion;
import io.github.jspinak.brobot.action.internal.capture.DefinedBorders;
import io.github.jspinak.brobot.action.internal.capture.RegionDefiner;
import io.github.jspinak.brobot.model.element.Region;

/**
 * Defines a region as the smallest rectangle that contains all specified anchor points.
 *
 * <p>This class implements an "inside" anchor strategy where the resulting region is the minimal
 * bounding box that encompasses all anchor points from matches and locations. This is useful when
 * you need to create a region that tightly fits around multiple GUI elements or reference points.
 *
 * <p>The algorithm works by:
 *
 * <ul>
 *   <li>Finding all matches in the provided object collections
 *   <li>Extracting anchor points from each match (or using default anchors)
 *   <li>Contracting the region boundaries inward to create the smallest possible rectangle
 *   <li>Only creating a region if all four borders (top, bottom, left, right) are defined
 * </ul>
 *
 * <p>Common use cases include:
 *
 * <ul>
 *   <li>Creating a region that encompasses a group of UI elements
 *   <li>Defining a working area based on multiple reference points
 *   <li>Establishing boundaries for subsequent automation operations
 * </ul>
 *
 * @see DefineOutsideAnchors
 * @see AnchorRegion
 * @see DefinedBorders
 * @see DefineRegionOptions.DefineAs#INSIDE_ANCHORS
 */
@Component
public class DefineInsideAnchors implements ActionInterface {

    @Override
    public Type getActionType() {
        return Type.DEFINE;
    }

    private final RegionDefiner defineHelper;
    private final AnchorRegion anchorRegion;

    private DefinedBorders definedBorders;

    public DefineInsideAnchors(RegionDefiner defineHelper, AnchorRegion anchorRegion) {
        this.defineHelper = defineHelper;
        this.anchorRegion = anchorRegion;
    }

    /**
     * Creates the smallest region that contains all anchor points from matches.
     *
     * <p>This method orchestrates the region definition process:
     *
     * <ol>
     *   <li>Uses DefineHelper to find all matches in the object collections
     *   <li>Initializes a new Region and DefinedBorders tracker
     *   <li>Delegates to AnchorRegion to fit the region to all anchor points
     *   <li>Applies any additional adjustments from DefineRegionOptions
     *   <li>Adds the region to ActionResult only if all borders are defined
     * </ol>
     *
     * <p>The region is only added to the result if all four borders (top, bottom, left, right) have
     * been defined by anchor points. This ensures that the region is fully bounded and prevents
     * partially defined regions from being used in automation.
     *
     * <p>The region boundaries are determined by the innermost anchor points in each direction,
     * creating the smallest possible rectangle that contains all anchors.
     *
     * @param matches The ActionResult containing DefineRegionOptions and to which the defined
     *     region will be added. The region is added only if fully defined. The output text is set
     *     to the region's string representation.
     * @param objectCollections The collections containing objects to find. Matches from these
     *     objects provide the anchor points for region definition.
     */
    @Override
    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        // Get the configuration
        ActionConfig config = matches.getActionConfig();
        DefineRegionOptions defineOptions = null;
        if (config instanceof DefineRegionOptions) {
            defineOptions = (DefineRegionOptions) config;
        }

        Region region = new Region();
        definedBorders = new DefinedBorders();
        defineHelper.findMatches(matches, objectCollections);

        // The DefinedBorders object keeps track of defined borders as the region is being defined
        anchorRegion.fitRegionToAnchors(definedBorders, region, matches);

        // Apply adjustments using V2 helper if we have DefineRegionOptions
        if (defineOptions != null) {
            defineHelper.adjust(region, defineOptions);
        }

        if (definedBorders.allBordersDefined()) {
            matches.addDefinedRegion(region);
        } // else return an undefined region instead of a partially defined region
        matches.setOutputText(region.toString());
    }
}
