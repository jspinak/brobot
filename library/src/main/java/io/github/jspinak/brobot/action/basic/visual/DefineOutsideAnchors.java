package io.github.jspinak.brobot.action.basic.visual;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.capture.RegionDefinitionHelper;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import org.springframework.stereotype.Component;

/**
 * Defines a region as the bounding box that encompasses all anchor points.
 * 
 * <p>This class implements an "outside" anchor strategy where the resulting region
 * expands to include all anchor points from matches and locations. Unlike
 * {@link DefineInsideAnchors} which creates the smallest rectangle, this creates
 * a bounding box by expanding outward to ensure all anchors are contained.</p>
 * 
 * <p>The algorithm works by:
 * <ul>
 *   <li>Initializing a 1x1 region at the first anchor point</li>
 *   <li>Expanding the region boundaries outward for each subsequent anchor</li>
 *   <li>Including any additional locations specified in ActionOptions</li>
 *   <li>Applying final adjustments from ActionOptions</li>
 * </ul>
 * </p>
 * 
 * <p>This strategy is useful when:
 * <ul>
 *   <li>You need a region that guarantees inclusion of all reference points</li>
 *   <li>Creating safe boundaries around a set of UI elements</li>
 *   <li>Defining an area for operations that must not miss any anchors</li>
 * </ul>
 * </p>
 * 
 * @see DefineInsideAnchors
 * @see DefineRegion
 * @see ActionOptions.DefineAs#OUTSIDE_ANCHORS
 */
@Component
public class DefineOutsideAnchors implements ActionInterface {

    private final RegionDefinitionHelper defineHelper;

    public DefineOutsideAnchors(RegionDefinitionHelper defineHelper) {
        this.defineHelper = defineHelper;
    }

    /**
     * Creates a region that expands to encompass all anchor points.
     * 
     * <p>This method orchestrates the region expansion process:
     * <ol>
     *   <li>Uses DefineHelper to find all matches in the object collections</li>
     *   <li>Returns early with appropriate message if no matches are found</li>
     *   <li>Initializes a region from the first anchor point</li>
     *   <li>Expands the region to include all other anchor points</li>
     *   <li>Further expands to include any specified locations</li>
     *   <li>Applies final adjustments from ActionOptions</li>
     *   <li>Adds the expanded region to the ActionResult</li>
     * </ol>
     * </p>
     * 
     * @param matches The ActionResult containing ActionOptions and to which the
     *                defined region will be added. Output text is set to describe
     *                the result or indicate no matches were found.
     * @param objectCollections The collections containing objects to find. Anchor
     *                          points from matches in these collections determine
     *                          the region boundaries.
     */
    @Override
    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        defineHelper.findMatches(matches, objectCollections);
        
        if (matches.isEmpty()) {
            matches.setOutputText("No matches found to define region");
            return;
        }
        
        Region region = initializeRegionFromFirstAnchor(matches);
        expandRegionToIncludeAllAnchors(region, matches);
        
        defineHelper.adjust(region, actionOptions);
        matches.addDefinedRegion(region);
        matches.setOutputText(region.toString());
    }
    
    /**
     * Initializes a 1x1 region positioned at the first available anchor point.
     * 
     * <p>This method examines the first match and creates the initial region at either:
     * <ul>
     *   <li>The first explicit anchor if the match has custom anchors defined</li>
     *   <li>The match's default anchor position if no custom anchors exist</li>
     * </ul>
     * </p>
     * 
     * @param matches The ActionResult containing matches (must not be empty)
     * @return A 1x1 Region positioned at the first anchor point
     */
    private Region initializeRegionFromFirstAnchor(ActionResult matches) {
        Match firstMatch = matches.getMatchList().get(0);
        
        // If the first match has anchors, use the first anchor's position
        if (!firstMatch.getAnchors().getAnchorList().isEmpty()) {
            Location anchorLocation = new Location(firstMatch, 
                firstMatch.getAnchors().getAnchorList().get(0).getPositionInMatch());
            return new Region(anchorLocation.getCalculatedX(), anchorLocation.getCalculatedY(), 1, 1);
        }
        
        // Otherwise, use the match's target location as the default anchor
        Location defaultLocation = firstMatch.getTarget();
        return new Region(defaultLocation.getCalculatedX(), defaultLocation.getCalculatedY(), 1, 1);
    }
    
    /**
     * Expands the region boundaries to include all anchor points from matches.
     * 
     * <p>For each match, this method:
     * <ul>
     *   <li>Processes all explicit anchors if defined</li>
     *   <li>Uses the match's default anchor if no explicit anchors exist</li>
     *   <li>Expands the region in the necessary directions to include each point</li>
     * </ul>
     * </p>
     * 
     * <p>The region is modified in-place through calls to {@link #expandRegionToPoint}.</p>
     * 
     * @param region The region to expand (modified in-place)
     * @param matches The ActionResult containing matches with anchor points
     */
    private void expandRegionToIncludeAllAnchors(Region region, ActionResult matches) {
        for (Match match : matches.getMatchList()) {
            // Process anchors if present
            if (!match.getAnchors().getAnchorList().isEmpty()) {
                match.getAnchors().getAnchorList().forEach(anchor -> {
                    Location anchorLocation = new Location(match, anchor.getPositionInMatch());
                    expandRegionToPoint(region, anchorLocation.getCalculatedX(), anchorLocation.getCalculatedY());
                });
            } else {
                // Use the match's target as the default anchor
                Location defaultLocation = match.getTarget();
                expandRegionToPoint(region, defaultLocation.getCalculatedX(), defaultLocation.getCalculatedY());
            }
        }
    }
    
    /**
     * Expands the region boundaries to include a specific point.
     * 
     * <p>This method modifies the region in-place by:
     * <ul>
     *   <li>Moving the left boundary left if the point is to the left</li>
     *   <li>Extending the width if the point is to the right</li>
     *   <li>Moving the top boundary up if the point is above</li>
     *   <li>Extending the height if the point is below</li>
     * </ul>
     * </p>
     * 
     * <p>The +1 adjustments ensure the point is fully contained within the region
     * boundaries, not just touching the edge.</p>
     * 
     * @param region The region to expand (modified in-place)
     * @param x The x-coordinate of the point to include
     * @param y The y-coordinate of the point to include
     */
    private void expandRegionToPoint(Region region, int x, int y) {
        // Expand left boundary if needed
        if (x < region.x()) {
            int widthIncrease = region.x() - x;
            region.setX(x);
            region.setW(region.w() + widthIncrease);
        }
        
        // Expand right boundary if needed
        if (x > region.x2()) {
            region.setW(x - region.x() + 1);
        }
        
        // Expand top boundary if needed
        if (y < region.y()) {
            int heightIncrease = region.y() - y;
            region.setY(y);
            region.setH(region.h() + heightIncrease);
        }
        
        // Expand bottom boundary if needed
        if (y > region.y2()) {
            region.setH(y - region.y() + 1);
        }
    }
}
