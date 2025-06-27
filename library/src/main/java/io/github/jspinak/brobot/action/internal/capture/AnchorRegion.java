package io.github.jspinak.brobot.action.internal.capture;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.visual.DefineInsideAnchors;
import io.github.jspinak.brobot.action.basic.visual.DefineOutsideAnchors;
import io.github.jspinak.brobot.action.basic.visual.DefineRegion;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateLocation;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Handles the adjustment of region boundaries based on anchor points from matched elements.
 * 
 * <p>This class is part of the Action Model (a) in the Brobot framework, specifically supporting
 * the region definition functionality within the capture package. It enables dynamic region
 * definition by using anchor points from found matches to establish the boundaries of a new region.</p>
 * 
 * <p>In the model-based automation paradigm, this class facilitates the creation of regions that
 * are relative to existing GUI elements, allowing for more flexible and adaptive automation scripts.
 * The anchor points can define one or two boundaries of the region depending on their position
 * (e.g., corner positions define two boundaries, while middle positions define only one).</p>
 * 
 * <p>Key functionality includes:
 * <ul>
 *   <li>Processing anchor points from multiple matches to define region boundaries</li>
 *   <li>Adjusting region coordinates based on anchor positions</li>
 *   <li>Supporting both absolute locations and match-relative anchor points</li>
 * </ul>
 * </p>
 * 
 * @see DefinedBorders
 * @see DefineRegion
 * @see DefineInsideAnchors
 * @see DefineOutsideAnchors
 */
@Component
public class AnchorRegion {

    /**
     * The anchors for each match are passed to the method that defines a boundary of the new region.
     * @param region the new region to define
     * @param actionResult the matches that are used to define the new region
     */
    public void fitRegionToAnchors(DefinedBorders definedBorders, Region region, ActionResult actionResult) {
        for (Match match : actionResult.getMatchList()) {
            // Match objects can have one or more anchors
            match.getAnchors().getAnchorList().forEach(anchor -> {
                definedBorders.setAsDefined(anchor.getAnchorInNewDefinedRegion());
                Location locationInMatch = new Location(match, anchor.getPositionInMatch());
                adjustDefinedRegion(region, locationInMatch, anchor.getAnchorInNewDefinedRegion());
            });
        }
    }

    /**
     * MIDDLE in this scenario is considered an unspecified value and is not used to define the region.
     * The other positions, LEFT RIGHT TOP BOTTOM, are considered specific and are used to define boundaries.
     * This is useful for matches that should define only the top, bottom, left, or right side of the new region
     * and not a specific point with x and y values.
     */
    private void adjustDefinedRegion(Region region, Location location, Positions.Name anchor) {
        int xDiff = location.getCalculatedX() - region.x();
        int x2Diff = location.getCalculatedX() - region.x2();
        int yDiff = location.getCalculatedY() - region.y();
        int y2Diff = location.getCalculatedY() - region.y2();
        if (anchor == Positions.Name.TOPLEFT) {
            if (xDiff > 0) region.adjustX(location.getCalculatedX());
            if (yDiff > 0) region.adjustY(location.getCalculatedY());
        }
        if (anchor == Positions.Name.MIDDLELEFT) {
            if (xDiff > 0) region.adjustX(location.getCalculatedX());
        }
        if (anchor == Positions.Name.BOTTOMLEFT) {
            if (xDiff > 0) region.adjustX(location.getCalculatedX());
            if (y2Diff < 0) region.adjustY2(location.getCalculatedY());
        }
        if (anchor == Positions.Name.TOPMIDDLE) {
            if (yDiff > 0) region.adjustY(location.getCalculatedY());
        }
        if (anchor == Positions.Name.TOPRIGHT) {
            if (x2Diff < 0) region.adjustX2(location.getCalculatedX());
            if (yDiff > 0) region.adjustY(location.getCalculatedY());
        }
        if (anchor == Positions.Name.MIDDLERIGHT) {
            if (x2Diff < 0) region.adjustX2(location.getCalculatedX());
        }
        if (anchor == Positions.Name.BOTTOMRIGHT) {
            if (x2Diff < 0) region.adjustX2(location.getCalculatedX());
            if (y2Diff < 0) region.adjustY2(location.getCalculatedY());
        }
        if (anchor == Positions.Name.BOTTOMMIDDLE) {
            if (y2Diff < 0) region.adjustY2(location.getCalculatedY());
        }
    }

    /**
     * Locations can also be used to define boundaries.
     * These are considered the same as anchors with x and y coordinates (not MIDDLE).
     */
    private void fitRegionToLocations(DefinedBorders definedBorders, Region region, List<StateLocation> locations) {
        for (StateLocation location : locations) {
            Location l = location.getLocation();
            definedBorders.setAsDefined(l.getAnchor());
            adjustDefinedRegion(region, l, l.getAnchor());
        }
    }
}
