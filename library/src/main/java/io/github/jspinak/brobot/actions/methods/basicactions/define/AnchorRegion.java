package io.github.jspinak.brobot.actions.methods.basicactions.define;

import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.location.Positions;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateLocation;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AnchorRegion {

    /**
     * The anchors for each match are passed to the method that defines a boundary of the new region.
     * @param region the new region to define
     * @param matches the matches that are used to define the new region
     */
    public void fitRegionToAnchors(DefinedBorders definedBorders, Region region, Matches matches) {
        for (Match match : matches.getMatchList()) {
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
        if (anchor == Positions.Name.TOPLEFT) {
            region.setX(Math.max(region.x(), location.getX()));
            region.setY(Math.max(region.y(), location.getY()));
        }
        if (anchor == Positions.Name.MIDDLELEFT) {
            region.setX(Math.max(region.x(), location.getX()));
        }
        if (anchor == Positions.Name.BOTTOMLEFT) {
            region.setX(Math.max(region.x(), location.getX()));
            region.setY2(Math.min(region.y2(), location.getY()));
        }
        if (anchor == Positions.Name.TOPMIDDLE) {
            region.setY(Math.max(region.y(), location.getY()));
        }
        if (anchor == Positions.Name.TOPRIGHT) {
            region.setX2(Math.min(region.x2(), location.getX()));
            region.setY(Math.max(region.y(), location.getY()));
        }
        if (anchor == Positions.Name.MIDDLERIGHT) {
            region.setX2(Math.min(region.x2(), location.getX()));
        }
        if (anchor == Positions.Name.BOTTOMRIGHT) {
            region.setX2(Math.min(region.x2(), location.getX()));
            region.setY2(Math.min(region.y2(), location.getY()));
        }
        if (anchor == Positions.Name.BOTTOMMIDDLE) {
            region.setY2(Math.min(region.y2(), location.getY()));
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
