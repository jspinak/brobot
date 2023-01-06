package io.github.jspinak.brobot.actions.methods.basicactions.define;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchObject;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateLocation;
import io.github.jspinak.brobot.reports.Report;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Defines a Region as the smallest rectangle produced by Matches and Locations.
 * Matches can contain Anchors that specify the point to use.
 *
 * <p>Author: Joshua Spinak</p>
 */
@Component
public class DefineInsideAnchors implements ActionInterface {

    private DefineHelper defineHelper;

    private DefinedBorders definedBorders;

    public DefineInsideAnchors(DefineHelper defineHelper) {
        this.defineHelper = defineHelper;
    }

    public Matches perform(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        Region region = new Region();
        definedBorders = new DefinedBorders();
        Matches matches = defineHelper.findMatches(actionOptions, objectCollections);
        // The DefinedBorders object keeps track of defined borders as the region is being defined
        fitRegionToAnchors(region, matches);
        //fitRegionToLocations(region, objectCollections[0].getStateLocations()); // locations are converted to Match objects in a Find operation
        defineHelper.adjust(region, actionOptions);
        if (definedBorders.allBordersDefined()) {
            matches.addDefinedRegion(region);
        } // else return an undefined region instead of a partially defined region
        return matches;
    }

    private void fitRegionToAnchors(Region region, Matches matches) {
        for (MatchObject matchObject : matches.getMatchObjects()) {
            Match match = matchObject.getMatch();
            // MatchObjects can have one or more anchors
            matchObject.getAnchorList().forEach(anchor -> {
                definedBorders.setAsDefined(anchor.getAnchorInNewDefinedRegion());
                Location locationInMatch = new Location(match, anchor.getLocationInMatch());
                adjustDefinedRegion(region, locationInMatch, anchor.getAnchorInNewDefinedRegion());
            });
        }
    }

    /**
     * MIDDLE in this scenario is considered an unspecified value and is not used to define the region.
     * The other positions, LEFT RIGHT TOP BOTTOM, are considered specific and are used to define boundaries.
     * This is useful for matches that should define a top, bottom, left, or right side only
     * and not a specific point with x and y values.
     */
    private void adjustDefinedRegion(Region region, Location location, Position.Name anchor) {
        if (anchor == Position.Name.TOPLEFT) {
            region.x = Math.max(region.x, location.getX());
            region.y = Math.max(region.y, location.getY());
        }
        if (anchor == Position.Name.MIDDLELEFT) {
            region.x = Math.max(region.x, location.getX());
        }
        if (anchor == Position.Name.BOTTOMLEFT) {
            region.x = Math.max(region.x, location.getX());
            region.setY2(Math.min(region.getY2(), location.getY()));
        }
        if (anchor == Position.Name.TOPMIDDLE) {
            region.y = Math.max(region.y, location.getY());
        }
        if (anchor == Position.Name.TOPRIGHT) {
            region.setX2(Math.min(region.getX2(), location.getX()));
            region.y = Math.max(region.y, location.getY());
        }
        if (anchor == Position.Name.MIDDLERIGHT) {
            region.setX2(Math.min(region.getX2(), location.getX()));
        }
        if (anchor == Position.Name.BOTTOMRIGHT) {
            region.setX2(Math.min(region.getX2(), location.getX()));
            region.setY2(Math.min(region.getY2(), location.getY()));
        }
        if (anchor == Position.Name.BOTTOMMIDDLE) {
            region.setY2(Math.min(region.getY2(), location.getY()));
        }
    }

    /**
     * Locations can also be used to define boundaries.
     * These are considered the same as anchors with x and y coordinates (not MIDDLE).
     *
     * @param region
     * @param locations
     */
    private void fitRegionToLocations(Region region, List<StateLocation> locations) {
        for (StateLocation location : locations) {
            Location l = location.getLocation();
            definedBorders.setAsDefined(l.getAnchor());
            adjustDefinedRegion(region, l, l.getAnchor());
        }
    }
}
