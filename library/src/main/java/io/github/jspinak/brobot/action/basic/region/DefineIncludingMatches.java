package io.github.jspinak.brobot.action.basic.region;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.capture.RegionDefiner;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;

/**
 * Defines a region as the bounding box that encompasses all found matches.
 *
 * <p>This class creates a single region that includes all matches found in the provided object
 * collections. Unlike anchor-based strategies that use specific points, this approach considers the
 * full bounds of each match to create an all-encompassing region.
 *
 * <p>The algorithm calculates the minimal bounding box by:
 *
 * <ul>
 *   <li>Finding the leftmost x-coordinate among all matches
 *   <li>Finding the topmost y-coordinate among all matches
 *   <li>Finding the rightmost x-coordinate among all matches
 *   <li>Finding the bottommost y-coordinate among all matches
 * </ul>
 *
 * <p>This strategy is particularly useful when:
 *
 * <ul>
 *   <li>You need to capture a dynamic area containing multiple UI elements
 *   <li>The exact positions of elements may vary but they form a logical group
 *   <li>You want to ensure all matched elements are within the defined region
 * </ul>
 *
 * @see DefineRegion
 * @see DefineRegionOptions.DefineAs#INCLUDING_MATCHES
 */
@Component
public class DefineIncludingMatches implements ActionInterface {

    @Override
    public Type getActionType() {
        return Type.DEFINE;
    }

    private final RegionDefiner defineHelper;

    public DefineIncludingMatches(RegionDefiner defineHelper) {
        this.defineHelper = defineHelper;
    }

    /**
     * Creates a region that encompasses all found matches.
     *
     * <p>This method executes the following steps:
     *
     * <ol>
     *   <li>Uses DefineHelper to find all matches in the object collections
     *   <li>Calculates the bounding box that includes all matches
     *   <li>Applies any adjustments specified in DefineRegionOptions
     *   <li>Adds the defined region to the ActionResult
     * </ol>
     *
     * <p>If no matches are found, an empty region (0,0,0,0) is created and added to the result
     * after adjustments are applied.
     *
     * @param matches The ActionResult containing DefineRegionOptions and to which the defined
     *     region will be added. This object is mutated by adding the encompassing region.
     * @param objectCollections The collections containing objects to find. All matches from these
     *     collections are included in the bounding box.
     */
    @Override
    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        // Get the configuration
        ActionConfig config = matches.getActionConfig();
        DefineRegionOptions defineOptions = null;
        if (config instanceof DefineRegionOptions) {
            defineOptions = (DefineRegionOptions) config;
        }

        defineHelper.findMatches(matches, objectCollections);
        Region region = fitRegionToMatches(matches);

        // Apply adjustments using V2 helper if we have DefineRegionOptions
        if (defineOptions != null) {
            defineHelper.adjust(region, defineOptions);
        }

        matches.addDefinedRegion(region);
    }

    /**
     * Calculates the bounding box that encompasses all matches.
     *
     * <p>This method iterates through all matches to find the extreme coordinates in each
     * direction. It uses the top-left corner for minimum coordinates and the top-right/bottom-left
     * corners to find maximum x and y coordinates respectively.
     *
     * <p>The calculation ensures that the entire area of each match is included, not just their
     * center points or anchor positions.
     *
     * @param matches The ActionResult containing the matches to bound
     * @return A Region that encompasses all matches, or an empty Region if no matches exist
     */
    private Region fitRegionToMatches(ActionResult matches) {
        if (matches.isEmpty()) return new Region();
        Match firstMatch = matches.getMatchList().get(0);
        int x = firstMatch.x();
        int y = firstMatch.y();
        int x2 = firstMatch.getRegion().sikuli().getTopRight().x;
        int y2 = firstMatch.getRegion().sikuli().getBottomLeft().y;
        for (int i = 1; i < matches.getMatchList().size(); i++) {
            Match match = matches.getMatchList().get(i);
            x = Math.min(x, match.x());
            y = Math.min(y, match.y());
            x2 = Math.max(x2, match.getRegion().sikuli().getTopRight().x);
            y2 = Math.max(y2, match.getRegion().sikuli().getBottomLeft().y);
        }
        return new Region(x, y, x2 - x + 1, y2 - y + 1);
    }
}
